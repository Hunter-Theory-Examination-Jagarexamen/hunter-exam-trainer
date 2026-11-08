import apiClient from "../api/apiClient";
import type { LearnerProgress, LearnerSummary } from "../types/learnerProgress";

// Admin pages for users and their progress (see the API spec, /api/admin/learners).
const endpoint = "/api/admin/learners";

// Every user as a light list (no email or results).
export const getLearners = (): Promise<LearnerSummary[]> => apiClient(endpoint);

// One user's progress, for the detail view.
export const getLearnerProgress = (userId: number): Promise<LearnerProgress> =>
    apiClient(`${endpoint}/${userId}`);
