import "../../styles/profile.css";
import { Share2 } from "lucide-react";
import { useEffect, useState } from "react";
import * as React from "react";
import { useTranslation } from "react-i18next";
import { getResultSharing, updateResultSharing } from "../../services/resultSharingService";
import type { ResultSharingPreferences } from "../../types/resultSharing";

/**
 * The student's sharing choices (#125): which of their own results course
 * leaders may see. The stored preferences are loaded on mount and replaced on
 * save, so what is shown after a refresh is what the server has stored.
 * The admin side is filtered on the server; this section only controls it.
 */
const ResultSharingSettings = () => {
    const { t } = useTranslation();

    const [preferences, setPreferences] = useState<ResultSharingPreferences | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        getResultSharing()
            .then(data => {
                if (active) setPreferences(data);
            })
            .catch(() => {
                if (active) setError(t("profile.resultSharingLoadError"));
            })
            .finally(() => {
                if (active) setIsLoading(false);
            });
        return () => { active = false; };
    }, [t]);

    const toggle = (key: keyof ResultSharingPreferences) => {
        setPreferences(current => current ? { ...current, [key]: !current[key] } : current);
        setMessage("");
        setError("");
    };

    const handleSave = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        if (!preferences) return;

        setMessage("");
        setError("");
        setIsSaving(true);
        try {
            const saved = await updateResultSharing(preferences);
            setPreferences(saved);
            setMessage(t("profile.resultSharingSuccess"));
        }
        catch {
            setError(t("profile.resultSharingSaveError"));
        }
        finally {
            setIsSaving(false);
        }
    };

    return (
        <section className="profile-section">
            <h2>{t("profile.resultSharingTitle")}</h2>
            <p className="result-sharing-intro">{t("profile.resultSharingText")}</p>

            {isLoading && <p role="status">{t("profile.resultSharingLoading")}</p>}
            {error && <p role="alert">{error}</p>}

            {preferences && (
                <form className="result-sharing-form" onSubmit={handleSave}>
                    <label className="result-sharing-option">
                        <input
                            type="checkbox"
                            checked={preferences.shareSubjectStatistics}
                            onChange={() => toggle("shareSubjectStatistics")}
                        />
                        <span>{t("profile.shareSubjectStatisticsLabel")}</span>
                    </label>

                    <label className="result-sharing-option">
                        <input
                            type="checkbox"
                            checked={preferences.shareMockExamResults}
                            onChange={() => toggle("shareMockExamResults")}
                        />
                        <span>{t("profile.shareMockExamResultsLabel")}</span>
                    </label>

                    {message && <p className="password-success" role="status">{message}</p>}

                    <div className="change-password-actions">
                        <button type="submit" className="btn btn-primary" disabled={isSaving}>
                            <Share2 size={16} />
                            {isSaving
                                ? t("profile.resultSharingSaving")
                                : t("profile.resultSharingSaveButton")}
                        </button>
                    </div>
                </form>
            )}
        </section>
    );
};

export default ResultSharingSettings;
