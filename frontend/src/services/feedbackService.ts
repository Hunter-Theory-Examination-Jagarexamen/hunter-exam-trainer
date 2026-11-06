import apiClient from "../api/apiClient";
import {errorStatus, isServerUnreachable} from "./apiErrors";
import type {FeedbackFilter, FeedbackItem, FeedbackRequest} from "../types/feedback";

export const sendFeedback = (request: FeedbackRequest): Promise<void> =>
    apiClient("/api/feedback", {method: "POST", body: JSON.stringify(request)});

export const listFeedback = (filter: FeedbackFilter = {}): Promise<FeedbackItem[]> => {
    // Only the filters that are set go into the URL, e.g. ?type=QUESTION&handled=false
    const params = new URLSearchParams();
    if (filter.type) params.set("type", filter.type);
    if (filter.handled !== undefined) params.set("handled", String(filter.handled));
    const query = params.toString();
    return apiClient(`/api/admin/feedback${query ? `?${query}` : ""}`);
};

export const setFeedbackHandled = (id: number, handled: boolean): Promise<FeedbackItem> =>
    apiClient(`/api/admin/feedback/${id}/handled`,
        {method: "PATCH", body: JSON.stringify({handled})});

// The translation key for a failed send, picked from the HTTP status
// (not from the wording of the backend's message).
export function sendFeedbackErrorKey(error: unknown): string {
    if (isServerUnreachable(error)) return "feedback.errors.unreachable";
    if (errorStatus(error) === 429) return "feedback.errors.tooMany";
    return "feedback.errors.sendFailed";
}
