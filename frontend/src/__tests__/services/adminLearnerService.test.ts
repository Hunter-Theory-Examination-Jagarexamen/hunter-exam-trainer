import { beforeEach, describe, expect, it, vi } from "vitest";
import apiClient from "../../api/apiClient";
import { getLearnerProgress, getLearners, updateUserRole } from "../../services/adminLearnerService";

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

    it("changes a user's role with the requested role as the body", async () => {
        const summary = { userId: 7, fullName: "Anna", role: "ADMIN" };
        apiClientMock.mockResolvedValue(summary);

        expect(await updateUserRole(7, "ADMIN")).toEqual(summary);
        expect(apiClientMock).toHaveBeenCalledWith("/api/admin/users/7/role", {
            method: "PUT",
            body: JSON.stringify({ role: "ADMIN" }),
        });
    });

    it("sends the demotion the same way", async () => {
        apiClientMock.mockResolvedValue({ userId: 9, fullName: "Bo", role: "STUDENT" });

        await updateUserRole(9, "STUDENT");
        expect(apiClientMock).toHaveBeenCalledWith("/api/admin/users/9/role", {
            method: "PUT",
            body: JSON.stringify({ role: "STUDENT" }),
        });
    });

    it("lets backend rejections through (409 last admin, 404 unknown user)", async () => {
        apiClientMock.mockRejectedValue(new Error(JSON.stringify({ message: "The last administrator cannot be demoted." })));
        await expect(updateUserRole(2, "STUDENT")).rejects.toThrow("The last administrator cannot be demoted.");
    });
});
