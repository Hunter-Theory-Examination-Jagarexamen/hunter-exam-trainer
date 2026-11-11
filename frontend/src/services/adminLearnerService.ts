import apiClient from "../api/apiClient";
import type { LearnerProgress, LearnerSummary } from "../types/learnerProgress";

// Admin pages for users and their progress (see the API spec, /api/admin/learners).
const endpoint = "/api/admin/learners";

// Every user as a light list (no email or results).
export const getLearners = (): Promise<LearnerSummary[]> => apiClient(endpoint);

// One user's progress, for the detail view.
export const getLearnerProgress = (userId: number): Promise<LearnerProgress> =>
    apiClient(`${endpoint}/${userId}`);

/**
 * Permanently deletes a student account (GDPR right to erasure, #194).
 * Admin-only. Rejects with 403 if the target is an admin; the frontend
 * should also hide the button in that case.
 */
export const deleteStudent = (userId: number): Promise<void> =>
    apiClient(`/api/admin/users/${userId}`, { method: "DELETE" });
