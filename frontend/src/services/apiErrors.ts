// Helpers for reading errors thrown by apiClient (an ApiError: the response body as
// message, plus the HTTP status). Kept outside apiClient.ts so tests that mock
// apiClient can still use them.

// The HTTP status, or undefined when there was no response (e.g. the network is down).
// Checks for a "status" field instead of instanceof ApiError, which also works with mocks.
export function errorStatus(error: unknown): number | undefined {
    return error instanceof Error && "status" in error && typeof error.status === "number"
        ? error.status : undefined;
}

// The backend sends {"message": "..."}; returns that message, or undefined if there is none.
export function backendMessage(error: unknown): string | undefined {
    if (!(error instanceof Error)) return undefined;
    try {
        const body: unknown = JSON.parse(error.message);
        if (typeof body === "object" && body !== null && "message" in body && typeof body.message === "string") {
            return body.message;
        }
    } catch { /* Not JSON, e.g. an empty body. */ }
    return undefined;
}

// True when the backend gave no usable answer (see ServerUnreachableError in apiClient.ts).
// Checks the name instead of instanceof, which also works with mocks.
export function isServerUnreachable(error: unknown): boolean {
    return error instanceof Error && error.name === "ServerUnreachableError";
}
