import { useSyncExternalStore } from "react";
import { getServerStatus, subscribeServerStatus } from "../../api/serverStatus";
import "../../styles/server-status.css";

// One shared message for the whole app when the backend is slow to answer
// or can't be reached. The pages keep their own "Loading…" and
// "Failed to load …" messages; this explains why.
const ServerStatusBanner = () => {
    // Re-renders this component whenever the server status changes.
    const status = useSyncExternalStore(subscribeServerStatus, getServerStatus);

    if (status === "ok") return null;

    if (status === "starting") {
        return (
            <div className="server-status-banner server-status-banner--starting" role="status">
                The server is starting. This can take up to a minute…
            </div>
        );
    }

    return (
        <div className="server-status-banner" role="alert">
            <span>
                Can't reach the server. Check your internet connection, or try again in a few minutes.
            </span>
            <button type="button" onClick={() => window.location.reload()}>
                Try again
            </button>
        </div>
    );
};

export default ServerStatusBanner;
