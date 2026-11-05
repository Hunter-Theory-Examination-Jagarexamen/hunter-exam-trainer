import {beforeEach, describe, expect, it, vi} from "vitest";
import apiClient from "../../api/apiClient";
import {
    answerPracticeQuestion,
    finishPracticeRound,
    startPracticeRound,
    startRandomPracticeRound
} from "../../services/practiceAttemptService";

vi.mock("../../api/apiClient", () => ({default: vi.fn()}));
const apiClientMock = vi.mocked(apiClient);

beforeEach(() => apiClientMock.mockReset());

describe("Practice attempt service", () => {
    it("starts a round for a subject", async () => {
        apiClientMock.mockResolvedValue({attemptId: 10, questions: []});
        expect(await startPracticeRound(2)).toEqual({attemptId: 10, questions: []});
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts",
            {method: "POST", body: JSON.stringify({subjectId: 2})});
    });

    it("starts a random round with no body, chosen by the server", async () => {
        apiClientMock.mockResolvedValue({attemptId: 11, subjectName: null, questions: []});
        expect(await startRandomPracticeRound()).toEqual({attemptId: 11, subjectName: null, questions: []});
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/random", {method: "POST"});
    });

    it("sends one answer and returns the server's check", async () => {
        const check = {correct: false, correctOption: "B", explanation: null};
        apiClientMock.mockResolvedValue(check);
        expect(await answerPracticeQuestion(10, 8, "A")).toEqual(check);
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/10/answers",
            {method: "POST", body: JSON.stringify({questionId: 8, chosenOption: "A"})});
    });

    it("finishes a round", async () => {
        apiClientMock.mockResolvedValue({correctAnswers: 5, answeredQuestions: 6, totalQuestions: 7});
        expect(await finishPracticeRound(10)).toEqual({correctAnswers: 5, answeredQuestions: 6, totalQuestions: 7});
        expect(apiClientMock).toHaveBeenCalledWith("/api/practice/attempts/10/finish", {method: "POST"});
    });
});
