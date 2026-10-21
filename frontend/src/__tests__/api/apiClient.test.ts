// Tests for src/api/apiClient.ts

import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import apiClient, { ApiError } from "../../api/apiClient.ts";

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