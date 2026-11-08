import apiClient from "../api/apiClient";
import {backendMessage, errorStatus} from "./apiErrors";
import i18n from "../i18n/config";
import type {Subject} from "../types/subject";

export interface SubjectRequest {
    name: string;
    description: string;
}

export const listSubjects = (): Promise<Subject[]> => apiClient("/api/subjects");
export const createSubject = (request: SubjectRequest): Promise<Subject> =>
    apiClient("/api/admin/subjects", {method: "POST", body: JSON.stringify(request)});
export const updateSubject = (id: number, request: SubjectRequest): Promise<Subject> =>
    apiClient(`/api/admin/subjects/${id}`, {method: "PUT", body: JSON.stringify(request)});
export const deleteSubject = async (id: number): Promise<void> => {
    await apiClient(`/api/admin/subjects/${id}`, {method: "DELETE"});
};

// Picks the user message from the HTTP status (see "Error responses" in the README),
// not from the wording of the backend's message.
export function subjectError(error: unknown, operation: "load" | "save" | "delete"): string {
    const fallback = i18n.t("subjects.errors.requestFailed");
    const status = errorStatus(error);
    if (status === undefined) {
        // No HTTP response at all, e.g. the network is down.
        return operation === "load" ? i18n.t("subjects.errors.loadFailed") : fallback;
    }
    const message = backendMessage(error);

    switch (status) {
        case 401:
        case 403:
            return i18n.t("subjects.errors.noPermission");
        case 404:
            return i18n.t("subjects.errors.notFound");
        case 409:
            return operation === "delete"
                ? i18n.t("subjects.errors.inUse")
                : i18n.t("subjects.errors.nameTaken");
        case 400:
            if (message === "Name is required") return i18n.t("subjects.editor.errors.missingName");
            if (message === "Description is required") return i18n.t("subjects.editor.errors.missingDescription");
            // Other 400 messages are our own validation texts, safe to show.
            return message ?? fallback;
    }
    // 500 messages are safe too and contain the reference code from the server log.
    if (status >= 500 && message) return message;
    return operation === "load" ? i18n.t("subjects.errors.loadFailed") : fallback;
}

