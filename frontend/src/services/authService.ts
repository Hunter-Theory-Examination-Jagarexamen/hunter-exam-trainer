import apiClient from "../api/apiClient";
import { backendMessage, isServerUnreachable } from "./apiErrors";

// All calls go through apiClient, so they get its time limit and its
// "server can't be reached" handling (ServerUnreachableError).

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
    tokenType: string;
}

export const loginUser = async (
    request: LoginRequest
): Promise<LoginResponse> => {
    return apiClient("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(request),
    });
};

export const checkGoogleLoginEnabled = async (): Promise<boolean> => {
    try {
        const data = await apiClient("/api/auth/google/status");
        return data?.enabled === true;
    } catch {
        return false;
    }
};

export interface ForgotPasswordResponse {
    message: string;
}

export const forgotPassword = async (
    email: string
): Promise<ForgotPasswordResponse> => {
    try {
        return await apiClient("/api/auth/forgot-password", {
            method: "POST",
            body: JSON.stringify({email}),
        });
    } catch (error) {
        if (isServerUnreachable(error)) throw error;
        throw new Error("Unable to process the request", {cause: error});
    }
};

export const resetPassword = async (
    token: string,
    newPassword: string
): Promise<{message: string}> => {
    try {
        return await apiClient("/api/auth/reset-password", {
            method: "POST",
            body: JSON.stringify({token, newPassword}),
        });
    } catch (error) {
        if (isServerUnreachable(error)) throw error;
        throw new Error(backendMessage(error) || "Unable to reset password. Please try again.", {cause: error});
    }
};
