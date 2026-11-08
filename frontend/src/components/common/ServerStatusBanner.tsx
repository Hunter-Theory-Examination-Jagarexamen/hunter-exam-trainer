import { useEffect, useState, useSyncExternalStore } from "react";
import { useTranslation } from "react-i18next";
import { getServerStatus, setServerStatus, subscribeServerStatus } from "../../api/serverStatus";
import { API_BASE_URL } from "../../api/apiClient";
import "../../styles/server-status.css";

// One shared message for the whole app when the backend is slow to answer
// or can't be reached. The pages keep their own "Loading…" and
// "Failed to load …" messages; this explains why.
const ServerStatusBanner = () => {
    const { t } = useTranslation();
    // Re-renders this component whenever the server status changes.
    const status = useSyncExternalStore(subscribeServerStatus, getServerStatus);
    const [isChecking, setIsChecking] = useState(false);
    const [stillDown, setStillDown] = useState(false);

    useEffect(() => {
        if (status === "ok") setStillDown(false);
    }, [status]);

    // "Try again" first checks that the server answers, then reloads (#188).
    // A bare no-cors request needs no endpoint: it only fails when the
    // server can't be reached at all.
    const handleTryAgain = async () => {
        setIsChecking(true);
        setStillDown(false);
        try {
            await fetch(API_BASE_URL, { mode: "no-cors", cache: "no-store" });
        } catch {
            setStillDown(true);
            setIsChecking(false);
            return;
        }
        setIsChecking(false);

        // A reload would end a running mock exam (a new start closes it), so
        // on the exam page only hide the banner; the exam just continues.
        if (window.location.pathname === "/exam") {
            setServerStatus("ok");
            return;
        }
        // Other pages fetch their data again.
        window.location.reload();
    };

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
                {stillDown && <> {t("serverStatus.stillDown")}</>}
            </span>
            <button type="button" onClick={handleTryAgain} disabled={isChecking}>
                {t("serverStatus.tryAgain")}
            </button>
        </div>
    );
};

export default ServerStatusBanner;
