import {beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "../../api/apiClient";
import {
    listFeedback,
    sendFeedback,
    sendFeedbackErrorKey,
    setFeedbackHandled
} from "../../services/feedbackService";

vi.mock("../../api/apiClient", () => ({default: vi.fn()}));
const apiClientMock = vi.mocked(apiClient);

beforeEach(() => apiClientMock.mockReset());

describe("Feedback service", () => {
    it("sends a question report with its question id", async () => {
        apiClientMock.mockResolvedValue("");
        await sendFeedback({type: "QUESTION", text: "Fel svar", questionId: 12});
        expect(apiClientMock).toHaveBeenCalledWith("/api/feedback",
            {method: "POST", body: JSON.stringify({type: "QUESTION", text: "Fel svar", questionId: 12})});
    });

    it("lists all feedback when no filter is set", async () => {
        apiClientMock.mockResolvedValue([]);
        expect(await listFeedback()).toEqual([]);
        expect(apiClientMock).toHaveBeenCalledWith("/api/admin/feedback");
    });

    it("puts only the set filters in the URL", async () => {
        apiClientMock.mockResolvedValue([]);
        await listFeedback({type: "QUESTION", handled: false});
        expect(apiClientMock).toHaveBeenLastCalledWith("/api/admin/feedback?type=QUESTION&handled=false");
        await listFeedback({handled: true});
        expect(apiClientMock).toHaveBeenLastCalledWith("/api/admin/feedback?handled=true");
    });

    it("marks an item as handled", async () => {
        apiClientMock.mockResolvedValue({id: 3, handled: true});
        expect(await setFeedbackHandled(3, true)).toEqual({id: 3, handled: true});
        expect(apiClientMock).toHaveBeenCalledWith("/api/admin/feedback/3/handled",
            {method: "PATCH", body: JSON.stringify({handled: true})});
    });

    it("picks the error message key from the status", () => {
        const tooMany = Object.assign(new Error('{"message":"Too much"}'), {status: 429});
        const serverError = Object.assign(new Error("boom"), {status: 500});
        const unreachable = Object.assign(new Error("down"), {name: "ServerUnreachableError"});
        expect(sendFeedbackErrorKey(tooMany)).toBe("feedback.errors.tooMany");
        expect(sendFeedbackErrorKey(serverError)).toBe("feedback.errors.sendFailed");
        expect(sendFeedbackErrorKey(unreachable)).toBe("feedback.errors.unreachable");
    });
});
