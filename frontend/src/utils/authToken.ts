// Reads the login token (a JWT) saved in the browser, to check its expiry time
// before showing a protected page. Only for the user experience: the backend
// still checks every token itself.

export type TokenStatus = "missing" | "invalid" | "expired" | "valid";

// The token's middle part (the payload) as an object, or undefined if the
// token is not a readable JWT.
function readPayload(token: string): Record<string, unknown> | undefined {
    const parts = token.split(".");
    if (parts.length !== 3) return undefined;
    try {
        // JWTs use base64url ("-" and "_"); atob expects plain base64 ("+" and "/").
        const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
        const payload: unknown = JSON.parse(atob(base64));
        return typeof payload === "object" && payload !== null
            ? payload as Record<string, unknown> : undefined;
    } catch {
        return undefined;
    }
}

// "exp" is in seconds since 1970; Date.now() is in milliseconds.
// A token without "exp" counts as valid; the backend decides.
export function getTokenStatus(now: number = Date.now()): TokenStatus {
    const token = localStorage.getItem("token");
    if (!token) return "missing";
    const payload = readPayload(token);
    if (!payload) return "invalid";
    if (typeof payload.exp === "number" && payload.exp * 1000 <= now) return "expired";
    return "valid";
}
