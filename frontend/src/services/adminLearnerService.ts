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

/**
 * Promotes a student to administrator ("ADMIN") or demotes an administrator
 * to student ("STUDENT"). Admin-only; the backend rejects self-demotion,
 * demoting the last administrator and unknown ids (409/404), so those errors
 * come back as failed promises with the backend's message.
 */
export const updateUserRole = (
    userId: number,
    role: LearnerSummary["role"]
): Promise<LearnerSummary> =>
    apiClient(`/api/admin/users/${userId}/role`, {
        method: "PUT",
        body: JSON.stringify({ role }),
    });
