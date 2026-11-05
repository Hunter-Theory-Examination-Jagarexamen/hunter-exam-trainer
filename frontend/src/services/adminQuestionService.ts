import apiClient from "../api/apiClient";
import {backendMessage, errorStatus} from "./apiErrors";
import type {Question} from "../types/question";

export interface QuestionRequest {
    questionText: string;
    optionA: string;
    optionB: string;
    optionC: string;
    optionD: string;
    correctAnswer: string;
    explanation: string;
    imageUrl?: string | null;
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

// Picks the user message from the HTTP status (see "Error responses" in the README),
// not from the wording of the backend's message.
export function questionMutationError(error: unknown): string {
    const fallback = "The request failed. Please try again. If it persists, refresh the question bank or contact an administrator.";
    const status = errorStatus(error);
    if (status === undefined) return fallback; // no response, e.g. the network is down
    const message = backendMessage(error);

    switch (status) {
        case 401:
        case 403:
            return "You do not have permission to manage questions. Please sign in with an admin account.";
        case 404:
            // Either the question or its subject was deleted by someone else.
            return "This question or its subject no longer exists. Refresh the question bank before trying again.";
        case 400:
            // Our own validation texts, e.g. "Correct answer must match one of the option texts".
            return message ?? fallback;
    }
    // 500 messages are safe too and contain the reference code from the server log.
    if (status >= 500 && message) return message;
    return fallback;
}
