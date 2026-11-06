import apiClient from "../api/apiClient";
import {errorStatus} from "./apiErrors";
import type {AdminStats} from "../types/adminStats";
import i18n from "../i18n/config";

const endpoint = "/api/admin/stats";

export const getAdminStats = (): Promise<AdminStats> => apiClient(endpoint);

// Picks the user message from the HTTP status (see "Error responses" in the README),
// not from the wording of the backend's message.
export function adminStatsError(error: unknown): string {
    const status = errorStatus(error);
    if (status === 401 || status === 403) {
        return i18n.t("admin.errors.noPermission");
    }
    return i18n.t("admin.errors.loadFailed");
}
