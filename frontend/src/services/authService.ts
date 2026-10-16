const API_URL = `${import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"}/api`;

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

    const response = await fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: {"Content-Type": "application/json",},
        body: JSON.stringify(request),
    });

    if (!response.ok) {
        throw new Error("Invalid email or password");
    }

    return response.json();
};


export const checkGoogleLoginEnabled = async (): Promise<boolean> => {

    try {
        const response = await fetch(`${API_URL}/auth/google/status`);

        if (!response.ok) {
            return false;
        }

        const data = await response.json();
        return data.enabled === true;
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

    const response = await fetch(`${API_URL}/auth/forgot-password`, {
        method: "POST",
        headers: {"Content-Type": "application/json",},
        body: JSON.stringify({email}),
    });

    if (!response.ok) {
        throw new Error("Unable to process the request");
    }

    return response.json();
};

export const resetPassword = async (
    token: string,
    newPassword: string
): Promise<{message: string}> => {
    const response = await fetch(`${API_URL}/auth/reset-password`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({token, newPassword}),
    });
    const data = await response.json();
    if (!response.ok) {
        throw new Error(data.message || "Unable to reset password. Please try again.");
    }
    return data;
};
