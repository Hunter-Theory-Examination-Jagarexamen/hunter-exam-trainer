import apiClient from "./apiClient";

/** Deletes the authenticated user's own account using their password. */
export const deleteMyAccount = (password: string): Promise<void> =>
    apiClient("/api/users/me", {
        method: "DELETE",
        body: JSON.stringify({ password }),
    });
