const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const apiClient = async (
    endpoint: string,
    options: RequestInit = {}
)=> {

    const token = localStorage.getItem("token");

    const headers = new Headers(options.headers);

    if (!headers.has("Content-Type")) {

        headers.set("Content-Type", "application/json");
    }

    if (token) {

        headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        ...options,
        headers,
    });

    if (!response.ok) {

        const errorMessage = await response.text();
        throw new Error(
            errorMessage || `API request failed: ${response.status}`
        );
    }

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get("content-type");

    if(contentType?.includes("application/json")) {
        return response.json();
    }

    return response.text();
};

export default apiClient;