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