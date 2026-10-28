import apiClient from "../api/apiClient";
import type {
    OptionLetter,
    PracticeAnswerResponse,
    PracticeFinishResponse,
    PracticeStartResponse
} from "../types/practiceAttempt";

// Practice rounds checked by the server (see "Practice" in docs/APIs/API-Specification.md).
const endpoint = "/api/practice/attempts";

export const startPracticeRound = (subjectId: number): Promise<PracticeStartResponse> =>
    apiClient(endpoint, {method: "POST", body: JSON.stringify({subjectId})});

export const answerPracticeQuestion = (
    attemptId: number, questionId: number, chosenOption: OptionLetter
): Promise<PracticeAnswerResponse> =>
    apiClient(`${endpoint}/${attemptId}/answers`,
        {method: "POST", body: JSON.stringify({questionId, chosenOption})});

export const finishPracticeRound = (attemptId: number): Promise<PracticeFinishResponse> =>
    apiClient(`${endpoint}/${attemptId}/finish`, {method: "POST"});
