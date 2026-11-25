// Whether the backend answers, shared by the whole app. apiClient updates it
// after every request; ServerStatusBanner shows it. A tiny "store": a value
// plus a list of listeners to call when it changes (used with useSyncExternalStore).

// "starting": the backend is probably waking up (Render puts it to sleep when
// unused). Set when a request has waited a few seconds without an answer,
// when a request failed, or when the host answered 502/503/504.
// ServerStatusBanner turns it red ("unreachable") after about 90 s.
export type ServerStatus = "ok" | "starting" | "unreachable";

let status: ServerStatus = "ok";
const listeners = new Set<() => void>();

export function getServerStatus(): ServerStatus {
    return status;
}

export function setServerStatus(next: ServerStatus): void {
    if (next === status) return;
    status = next;
    listeners.forEach(listener => listener());
}

// Returns a function that removes the listener again.
export function subscribeServerStatus(listener: () => void): () => void {
    listeners.add(listener);
    return () => listeners.delete(listener);
}
