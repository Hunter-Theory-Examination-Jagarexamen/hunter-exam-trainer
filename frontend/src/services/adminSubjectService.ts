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

// Never display database exception text from the backend's catch-all handler.
export function subjectError(error: unknown, operation: "load" | "save" | "delete"): string {
    const fallback = "The request failed. Please try again. If it persists, contact an administrator.";
    if (!(error instanceof Error)) return fallback;
    const status = "status" in error && typeof error.status === "number" ? error.status : undefined;
    let message = error.message;
    try {
        const body: unknown = JSON.parse(message);
        if (typeof body === "object" && body !== null && "message" in body && typeof body.message === "string") {
            message = body.message;
        }
    } catch { /* Plain-text HTTP and network errors are also supported. */ }
    if (status === 401 || status === 403 || /401|403|unauthorized|forbidden|access denied/i.test(message)) {
        return "You do not have permission to manage subjects. Please sign in with an admin account.";
    }
    if (status === 404 || /subject not found/i.test(message)) {
        return "This subject no longer exists. Refresh the subject list before trying again.";
    }
    if (operation === "save") {
        if (/duplicate entry|duplicate key|unique constraint|already exists/i.test(message)) {
            return "A subject with this name already exists. Choose a different name.";
        }
        if (message === "Name is required") return "Enter a subject name.";
        if (message === "Description is required") return "Enter a subject description.";
        if (/data too long|value too long/i.test(message)) return "Name and description must each be at most 255 characters.";
    }
    if (operation === "delete" && /foreign key|referential integrity|still referenced|in use/i.test(message)) {
        return "This subject cannot currently be deleted because it is in use by questions or practice results. No subject was deleted.";
    }
    return operation === "load" ? "Failed to load subjects. Please try again." : fallback;
}
