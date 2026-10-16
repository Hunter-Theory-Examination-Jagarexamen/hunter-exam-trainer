import apiClient from "../api/apiClient";
import type {Subject} from "../types/subject";

export interface SubjectRequest {
    name: string;
    description: string;
}

export const listSubjects = (): Promise<Subject[]> => apiClient("/api/subjects");
export const createSubject = (request: SubjectRequest): Promise<Subject> =>
    apiClient("/api/subjects", {method: "POST", body: JSON.stringify(request)});
export const updateSubject = (id: number, request: SubjectRequest): Promise<Subject> =>
    apiClient(`/api/subjects/${id}`, {method: "PUT", body: JSON.stringify(request)});
export const deleteSubject = async (id: number): Promise<void> => {
    await apiClient(`/api/subjects/${id}`, {method: "DELETE"});
};

// Picks the user message from the HTTP status (see "Error responses" in the README),
// not from the wording of the backend's message.
export function subjectError(error: unknown, operation: "load" | "save" | "delete"): string {
    const fallback = "The request failed. Please try again. If it persists, contact an administrator.";
    // apiClient throws an ApiError with the HTTP status. Checking for a "status" field
    // (instead of instanceof) also works in tests that mock apiClient.
    const status = error instanceof Error && "status" in error && typeof error.status === "number"
        ? error.status : undefined;
    if (status === undefined) {
        // No HTTP response at all, e.g. the network is down.
        return operation === "load" ? "Failed to load subjects. Please try again." : fallback;
    }
    const message = backendMessage((error as Error).message);

    switch (status) {
        case 401:
        case 403:
            return "You do not have permission to manage subjects. Please sign in with an admin account.";
        case 404:
            return "This subject no longer exists. Refresh the subject list before trying again.";
        case 409:
            return operation === "delete"
                ? "This subject cannot currently be deleted because it is in use by questions or practice results. No subject was deleted."
                : "A subject with this name already exists. Choose a different name.";
        case 400:
            if (message === "Name is required") return "Enter a subject name.";
            if (message === "Description is required") return "Enter a subject description.";
            // Other 400 messages are our own validation texts, safe to show.
            return message ?? fallback;
    }
    // 500 messages are safe too and contain the reference code from the server log.
    if (status >= 500 && message) return message;
    return operation === "load" ? "Failed to load subjects. Please try again." : fallback;
}

// The backend sends {"message": "..."}; the client passes the raw body on as the error text.
function backendMessage(text: string): string | undefined {
    try {
        const body: unknown = JSON.parse(text);
        if (typeof body === "object" && body !== null && "message" in body && typeof body.message === "string") {
            return body.message;
        }
    } catch { /* Not JSON, e.g. an empty body. */ }
    return undefined;
}
