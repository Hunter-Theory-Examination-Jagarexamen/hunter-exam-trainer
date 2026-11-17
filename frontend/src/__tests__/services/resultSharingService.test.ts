import { beforeEach, describe, expect, it, vi } from "vitest";
import apiClient from "../../api/apiClient";
import { getResultSharing, updateResultSharing } from "../../services/resultSharingService";

vi.mock("../../api/apiClient", () => ({ default: vi.fn() }));
const apiClientMock = vi.mocked(apiClient);

beforeEach(() => vi.resetAllMocks());

describe("Result sharing service", () => {
    it("reads the student's own preferences", async () => {
        const preferences = { shareSubjectStatistics: false, shareMockExamResults: false };
        apiClientMock.mockResolvedValue(preferences);

        expect(await getResultSharing()).toEqual(preferences);
        expect(apiClientMock).toHaveBeenCalledWith("/api/users/me/result-sharing");
    });

    it("replaces the preferences with PUT and returns what the server stored", async () => {
        const preferences = { shareSubjectStatistics: true, shareMockExamResults: false };
        apiClientMock.mockResolvedValue(preferences);

        expect(await updateResultSharing(preferences)).toEqual(preferences);
        expect(apiClientMock).toHaveBeenCalledWith("/api/users/me/result-sharing", {
            method: "PUT",
            body: JSON.stringify(preferences),
        });
    });
});
