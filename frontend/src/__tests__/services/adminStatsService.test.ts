import {afterEach, describe, expect, it, vi} from "vitest";
import {adminStatsError, getAdminStats} from "../../services/adminStatsService";

afterEach(() => {
    vi.unstubAllGlobals();
    localStorage.clear();
});

describe("Admin stats service HTTP contract", () => {
    it("requests the admin stats endpoint with the authenticated client", async () => {
        localStorage.setItem("token", "admin-token");
        const response = {questionCount: 42, subjectCount: 5, studentCount: 17};
        const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(response),
            {status: 200, headers: {"Content-Type": "application/json"}}));
        vi.stubGlobal("fetch", fetchMock);

        const result = await getAdminStats();

        expect(fetchMock).toHaveBeenCalledTimes(1);
        const [url, options] = fetchMock.mock.calls[0];
        expect(url).toBe(`${import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"}/api/admin/stats`);
        expect(options.headers.get("Authorization")).toBe("Bearer admin-token");
        expect(result).toEqual(response);
    });

    it("turns a forbidden response into a permission message and anything else into a load failure", () => {
        const forbidden = Object.assign(new Error('{"message":"You do not have permission to do this"}'), {status: 403});
        expect(adminStatsError(forbidden))
            .toBe("You do not have permission to view the admin overview. Please sign in with an admin account.");
        expect(adminStatsError(new Error("network down")))
            .toBe("Failed to load the admin overview. Please try again.");
    });
});
