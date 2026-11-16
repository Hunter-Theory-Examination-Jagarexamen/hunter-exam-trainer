import apiClient from "../api/apiClient";
import type { ResultSharingPreferences } from "../types/resultSharing";

// The logged-in student's sharing choices (see the API spec,
// /api/users/me/result-sharing). The owner always comes from the token,
// so there is no id to pass around.
const endpoint = "/api/users/me/result-sharing";

export const getResultSharing = (): Promise<ResultSharingPreferences> =>
    apiClient(endpoint);

export const updateResultSharing = (
    preferences: ResultSharingPreferences
): Promise<ResultSharingPreferences> =>
    apiClient(endpoint, {
        method: "PUT",
        body: JSON.stringify(preferences),
    });
