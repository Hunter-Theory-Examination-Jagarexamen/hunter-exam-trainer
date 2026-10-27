import { setServerStatus } from "./serverStatus";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export class ApiError extends Error {
    readonly status: number;
    constructor(message: string, status: number) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

// How long to wait for an answer before giving up. A sleeping backend on
// Render can take about a minute to wake up.
export const REQUEST_TIMEOUT_MS = 90_000;

// Thrown when no usable answer comes from the backend: the request failed
// (offline, server down, blocked by CORS), took longer than
// REQUEST_TIMEOUT_MS, or the host answered that the server is unavailable.
export class ServerUnreachableError extends Error {
    constructor() {
        super("The server can't be reached. Please try again later.");
        this.name = "ServerUnreachableError";
    }
}

// Answers from the host (e.g. Render) when the backend itself is down or restarting.
const SERVER_UNAVAILABLE_STATUSES = [502, 503, 504];

// Login, register, password reset etc. These work without a login, so they get
// no token (an old token makes the backend answer 401 even here), and a 401
// from them means wrong input, not an expired login.
const isAuthEndpoint = (endpoint: string): boolean =>
    endpoint.startsWith("/api/auth/") || endpoint.startsWith("/auth/");

const handleSessionExpired = (): void => {
    localStorage.removeItem("token");
    if (!window.location.pathname.startsWith("/login")) {
        window.location.href = "/login?expired=1";
    }
};

const apiClient = async (
    endpoint: string,
    options: RequestInit = {}
): Promise<any> => {

    const token = localStorage.getItem("token");

    const headers = new Headers(options.headers);

    if (!headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }

    if (token && !isAuthEndpoint(endpoint)) {
        headers.set("Authorization", `Bearer ${token}`);
    }

    // AbortController lets us cancel the request ourselves when it takes too long.
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

    let response: Response;
    try {
        response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers,
            signal: controller.signal,
        });
    } catch {
        // fetch only throws when no answer came at all, or when we aborted it.
        setServerStatus("unreachable");
        throw new ServerUnreachableError();
    } finally {
        clearTimeout(timeoutId);
    }

    if (SERVER_UNAVAILABLE_STATUSES.includes(response.status)) {
        setServerStatus("unreachable");
        throw new ServerUnreachableError();
    }
    setServerStatus("ok");

    if (response.status === 401 && !isAuthEndpoint(endpoint)) {
        handleSessionExpired();
        throw new ApiError("Session expired. Please log in again.", 401);
    }

    if (!response.ok) {
        const errorMessage = await response.text();
        throw new ApiError(
            errorMessage || `API request failed: ${response.status}`,
            response.status
        );
    }

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get("content-type");

    if (contentType?.includes("application/json")) {
        return response.json();
    }

    return response.text();
};

export default apiClient;