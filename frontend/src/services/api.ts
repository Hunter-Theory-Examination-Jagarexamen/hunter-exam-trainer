const API_URL = `${import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"}/api`;

export const apiRequest = async (
    endpoint: string,
    options: RequestInit = {}
) => {

    const token = localStorage.getItem("token");

    const response = await fetch(`${API_URL}${endpoint}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...options.headers,
            ...(token && {
                Authorization: `Bearer ${token}`,
            }),
        },
    });

    if (!response.ok) {
        throw new Error(`API request failed: ${response.status}`);
    }

    return response;
};