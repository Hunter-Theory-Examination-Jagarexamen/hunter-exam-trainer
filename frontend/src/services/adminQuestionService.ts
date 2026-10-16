import apiClient from "../api/apiClient";
import type {Question} from "../types/question";

export interface QuestionRequest {
    questionText: string;
    optionA: string;
    optionB: string;
    optionC: string;
    optionD: string;
    correctAnswer: string;
    explanation: string;
    imageUrl?: string;
    subjectId: number;
}

const endpoint = "/api/admin/questions";

export const createQuestion = (request: QuestionRequest): Promise<Question> =>
    apiClient(endpoint, {method: "POST", body: JSON.stringify(request)});

export const updateQuestion = (id: number, request: QuestionRequest): Promise<Question> =>
    apiClient(`${endpoint}/${id}`, {method: "PUT", body: JSON.stringify(request)});

export const deleteQuestion = async (id: number): Promise<void> => {
    await apiClient(`${endpoint}/${id}`, {method: "DELETE"});
};

// The shared client throws the response body as text. Only expose known, useful
// errors; the backend catch-all can otherwise include technical exception details.
export function questionMutationError(error: unknown): string {
    const fallback = "The request failed. Please try again. If it persists, refresh the question bank or contact an administrator.";
    if (!(error instanceof Error)) return fallback;
    let message = error.message;
    try {
        const body: unknown = JSON.parse(message);
        if (typeof body === "object" && body !== null && "message" in body && typeof body.message === "string") {
            message = body.message;
        }
    } catch { /* Network failures and the client's status fallback are plain text. */ }
    if (/401|403|unauthorized|forbidden|access denied/i.test(message)) {
        return "You do not have permission to manage questions. Please sign in with an admin account.";
    }
    if (/question not found/i.test(message)) return "This question no longer exists. Refresh the question bank before trying again.";
    if (/subject not found/i.test(message)) return "This subject no longer exists. Refresh the question bank and choose an available subject.";
    if (message === "Correct answer must match one of the option texts") return message;
    return fallback;
}
