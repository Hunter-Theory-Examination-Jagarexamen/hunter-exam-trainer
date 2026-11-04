import { useSyncExternalStore } from "react";
import { useTranslation } from "react-i18next";
import { getServerStatus, subscribeServerStatus } from "../../api/serverStatus";
import "../../styles/server-status.css";

// One shared message for the whole app when the backend is slow to answer
// or can't be reached. The pages keep their own "Loading…" and
// "Failed to load …" messages; this explains why.
const ServerStatusBanner = () => {
    const { t } = useTranslation();
    // Re-renders this component whenever the server status changes.
    const status = useSyncExternalStore(subscribeServerStatus, getServerStatus);

    if (status === "ok") return null;

    if (status === "starting") {
        return (
            <div className="server-status-banner server-status-banner--starting" role="status">
                {t("serverStatus.starting")}
            </div>
        );
    }


    return (
        <div className="server-status-banner" role="alert">
            <span>
                {t("serverStatus.error")}
            </span>
            <button type="button" onClick={() => window.location.reload()}>
                {t("serverStatus.tryAgain")}
            </button>
        </div>
    );
};

export default ServerStatusBanner;
