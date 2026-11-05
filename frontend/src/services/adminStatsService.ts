import apiClient from "../api/apiClient";
import {errorStatus} from "./apiErrors";
import type {AdminStats} from "../types/adminStats";

const endpoint = "/api/admin/stats";

export const getAdminStats = (): Promise<AdminStats> => apiClient(endpoint);

// Picks the user message from the HTTP status (see "Error responses" in the README),
// not from the wording of the backend's message.
export function adminStatsError(error: unknown): string {
    const status = errorStatus(error);
    if (status === 401 || status === 403) {
        return "You do not have permission to view the admin overview. Please sign in with an admin account.";
    }
    return "Failed to load the admin overview. Please try again.";
}
