import {afterEach, describe, expect, it, vi} from "vitest";
import {forgotPassword, loginUser, resetPassword} from "../../services/authService";
import {ServerUnreachableError} from "../../api/apiClient";

// A real Response object, as fetch would return it.
function jsonResponse(body: unknown, status = 200) {
    return new Response(JSON.stringify(body), {status, headers: {"Content-Type": "application/json"}});
}

afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
});

describe("authService", () => {
    it("loginUser does not send an old saved token (the backend would answer 401)", async () => {
        localStorage.setItem("token", "old-token");
        const fetchMock = vi.fn().mockResolvedValue(jsonResponse({token: "new-token", tokenType: "Bearer"}));
        vi.stubGlobal("fetch", fetchMock);

        await expect(loginUser({email: "a@example.com", password: "pw"}))
            .resolves.toEqual({token: "new-token", tokenType: "Bearer"});

        const headers: Headers = fetchMock.mock.calls[0][1].headers;
        expect(headers.has("Authorization")).toBe(false);
    });

    it("loginUser throws ServerUnreachableError when the backend can't be reached", async () => {
        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));

        await expect(loginUser({email: "a@example.com", password: "pw"}))
            .rejects.toBeInstanceOf(ServerUnreachableError);
    });

    it("forgotPassword keeps ServerUnreachableError, and gives a general message for other errors", async () => {
        vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("Failed to fetch")));
        await expect(forgotPassword("a@example.com")).rejects.toBeInstanceOf(ServerUnreachableError);

        vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse({message: "Boom"}, 500)));
        await expect(forgotPassword("a@example.com")).rejects.toThrow("Unable to process the request");
    });

    it("resetPassword shows the backend's message when the reset is rejected", async () => {
        vi.stubGlobal("fetch", vi.fn().mockResolvedValue(jsonResponse({message: "The reset link has expired"}, 400)));

        await expect(resetPassword("token", "new-password")).rejects.toThrow("The reset link has expired");
    });
});
