import { useSyncExternalStore } from "react";
import { getServerStatus, subscribeServerStatus } from "../../api/serverStatus";
import "../../styles/server-status.css";

// One shared message for the whole app when the backend can't be reached.
// The pages keep their own "Failed to load …" messages; this explains why.
const ServerStatusBanner = () => {
    // Re-renders this component whenever the server status changes.
    const status = useSyncExternalStore(subscribeServerStatus, getServerStatus);

    if (status === "ok") return null;

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
