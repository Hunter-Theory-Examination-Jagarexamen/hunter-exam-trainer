import { beforeEach, describe, expect, it, vi } from "vitest";
import apiClient from "../../api/apiClient";
import { getLearnerProgress, getLearners } from "../../services/adminLearnerService";

vi.mock("../../api/apiClient", () => ({ default: vi.fn() }));
const apiClientMock = vi.mocked(apiClient);

beforeEach(() => vi.resetAllMocks());

describe("Admin learner service", () => {
    it("gets the list of users", async () => {
        const list = [{ userId: 5, fullName: "Zara Admin", role: "ADMIN" }];
        apiClientMock.mockResolvedValue(list);
        expect(await getLearners()).toEqual(list);
        expect(apiClientMock).toHaveBeenCalledWith("/api/admin/learners");
    });

    it("gets one user's progress by id", async () => {
        apiClientMock.mockResolvedValue({ userId: 7, fullName: "Anna" });
        expect(await getLearnerProgress(7)).toEqual({ userId: 7, fullName: "Anna" });
        expect(apiClientMock).toHaveBeenCalledWith("/api/admin/learners/7");
    });
});
