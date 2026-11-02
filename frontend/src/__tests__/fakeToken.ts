// Builds a JWT-shaped token for tests, expiring the given number of seconds
// from now (negative = already expired). The signature is fake; the frontend
// never checks it.
export function fakeToken(expiresInSeconds = 3600): string {
    const encode = (part: object) =>
        btoa(JSON.stringify(part)).replace(/=+$/, "").replace(/\+/g, "-").replace(/\//g, "_");
    const exp = Math.floor(Date.now() / 1000) + expiresInSeconds;
    return `${encode({alg: "HS256", typ: "JWT"})}.${encode({sub: "test@example.com", exp})}.fake-signature`;
}
