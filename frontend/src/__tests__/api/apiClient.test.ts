// Tests for src/api/apiClient.ts

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import apiClient, { ApiError, REQUEST_TIMEOUT_MS, STARTING_MESSAGE_AFTER_MS, ServerUnreachableError } from "../../api/apiClient.ts";
import { getServerStatus, setServerStatus } from "../../api/serverStatus.ts";
import { isServerUnreachable } from "../../services/apiErrors.ts";

describe("apiClient — session expiry handling", () => {

    let originalLocation: Location;

    beforeEach(() => {
        vi.clearAllMocks();
        // Fake window.location so we can assert redirect without navigating
        originalLocation = window.location;
        Object.defineProperty(window, "location", {
            configurable: true,
            value: { ...originalLocation, href: "", pathname: "/dashboard" },
        });
    });

    afterEach(() => {
        Object.defineProperty(window, "location", {
            configurable: true,
            value: originalLocation,
        });
        localStorage.clear();
        vi.unstubAllGlobals();
    });

    it("clears token and redirects when an authenticated request returns 401", async () => {
        // Arrange: an expired token in localStorage
        localStorage.setItem("token", "expired-token");

        // Mock fetch to return 401
        vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
            ok: false,
            status: 401,
            text: async () => "Unauthorized",
        }));

        // Act + Assert: apiClient throws
        await expect(apiClient("/api/users/me")).rejects.toThrow(ApiError);

        // Assert: token was actually removed (via localStorage state, not spy)
        expect(localStorage.getItem("token")).toBeNull();
        // Assert: redirect happened
        expect(window.location.href).toBe("/login?expired=1");
    });

    it("does NOT clear token or redirect on 401 from /api/auth/login", async () => {
        // Arrange: wrong credentials — 401 from login is expected
        localStorage.setItem("token", "some-token");

        vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
            ok: false,
            status: 401,
            text: async () => "Invalid email or password",
        }));

        // Act + Assert
        await expect(apiClient("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ email: "x", password: "y" }),
        })).rejects.toThrow(ApiError);

        // Assert: token is still there, no redirect
        expect(localStorage.getItem("token")).toBe("some-token");
        expect(window.location.href).toBe("");
    });

    it("does NOT clear token or redirect on 403", async () => {
        // 403 = logged in but not authorized — not a session expiry
        localStorage.setItem("token", "valid-student-token");

        vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
            ok: false,
            status: 403,
            text: async () => "Forbidden",
        }));

        await expect(apiClient("/api/admin/learners")).rejects.toThrow(ApiError);

        expect(localStorage.getItem("token")).toBe("valid-student-token");
        expect(window.location.href).toBe("");
    });

    it("does not redirect twice when already on /login", async () => {
        localStorage.setItem("token", "expired-token");
        Object.defineProperty(window, "location", {
            configurable: true,
            value: { ...window.location, href: "", pathname: "/login" },
        });

        vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
            ok: false,
            status: 401,
            text: async () => "Unauthorized",
        }));

        await expect(apiClient("/api/users/me")).rejects.toThrow(ApiError);

        // Token still cleared, but no redirect since already on /login
        expect(localStorage.getItem("token")).toBeNull();
        expect(window.location.href).toBe("");
    });
});

describe("apiClient — backend can't be reached", () => {

    afterEach(() => {
        vi.useRealTimers();
        vi.unstubAllGlobals();
    });

    it("throws ServerUnreachableError when fetch fails (offline, server down, CORS)", async () => {
        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));

        const error = await apiClient("/api/users/me").catch(e => e);

        expect(error).toBeInstanceOf(ServerUnreachableError);
        expect(isServerUnreachable(error)).toBe(true);
    });

    it.each([502, 503, 504])("throws ServerUnreachableError when the host answers %i", async status => {
        vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status, text: async () => "" }));

        await expect(apiClient("/api/users/me")).rejects.toBeInstanceOf(ServerUnreachableError);
    });

    it("gives up after REQUEST_TIMEOUT_MS when no answer comes", async () => {
        vi.useFakeTimers();
        // A fetch that never answers, but stops when the request is aborted.
        vi.stubGlobal("fetch", vi.fn((_url: string, init: RequestInit) => new Promise((_resolve, reject) => {
            init.signal?.addEventListener("abort", () => reject(new DOMException("Aborted", "AbortError")));
        })));

        let settled = false;
        const result = apiClient("/api/users/me").catch(e => { settled = true; return e; });

        await vi.advanceTimersByTimeAsync(REQUEST_TIMEOUT_MS - 1);
        expect(settled).toBe(false);

        await vi.advanceTimersByTimeAsync(1);
        expect(await result).toBeInstanceOf(ServerUnreachableError);
    });

    it("clears the time limit when the answer comes in time", async () => {
        vi.useFakeTimers();
        vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
            ok: true, status: 204, headers: new Headers(), text: async () => "",
        }));

        await apiClient("/api/users/me");

        expect(vi.getTimerCount()).toBe(0);
    });

    it("isServerUnreachable is false for other errors", () => {
        expect(isServerUnreachable(new ApiError("Forbidden", 403))).toBe(false);
        expect(isServerUnreachable("not an error")).toBe(false);
    });
});

describe("apiClient — slow backend (\"server is starting\")", () => {

    // A fetch that answers only when the test calls answer().
    function slowFetch() {
        const pending: ((response: Response) => void)[] = [];
        vi.stubGlobal("fetch", vi.fn(() => new Promise<Response>(resolve => pending.push(resolve))));
        return (index = 0) => pending[index](new Response(null, { status: 204 }));
    }

    beforeEach(() => {
        vi.useFakeTimers();
        setServerStatus("ok");
    });

    afterEach(() => {
        vi.useRealTimers();
        vi.unstubAllGlobals();
    });

    it("sets \"starting\" after STARTING_MESSAGE_AFTER_MS without an answer, and \"ok\" when it comes", async () => {
        const answer = slowFetch();
        const request = apiClient("/api/users/me");

        await vi.advanceTimersByTimeAsync(STARTING_MESSAGE_AFTER_MS - 1);
        expect(getServerStatus()).toBe("ok");

        await vi.advanceTimersByTimeAsync(1);
        expect(getServerStatus()).toBe("starting");

        answer();
        await request;
        expect(getServerStatus()).toBe("ok");
    });

    it("does not set \"starting\" when another request got an answer meanwhile", async () => {
        const answer = slowFetch();
        const slowRequest = apiClient("/api/dashboard");
        const fastRequest = apiClient("/api/users/me");

        answer(1);
        await fastRequest;
        await vi.advanceTimersByTimeAsync(STARTING_MESSAGE_AFTER_MS);
        expect(getServerStatus()).toBe("ok");

        answer(0);
        await slowRequest;
    });
});
