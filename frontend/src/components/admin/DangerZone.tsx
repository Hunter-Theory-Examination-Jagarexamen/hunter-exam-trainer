import { useCallback, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { deleteStudent } from "../../services/adminLearnerService";
import { backendMessage, errorStatus } from "../../services/apiErrors";
import "../../styles/admin.css";

interface DangerZoneProps {
    userId: number;
    userName: string;
}

/**
 * "Delete student account" section on the participant detail page (#232).
 * Reuses the backend endpoint from #194. Kept in its own component so the
 * upcoming promote/demote (#204) can sit next to it.
 */
const DangerZone = ({ userId, userName }: DangerZoneProps) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [isConfirming, setIsConfirming] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [password, setPassword] = useState("");
    const deletingRef = useRef(false);

    const closeDialog = useCallback(() => {
        setIsConfirming(false);
        setError(null);
        setPassword("");
    }, []);

    // Escape closes the confirmation (matches FeedbackDialog / LeaveExamDialog).
    useEffect(() => {
        if (!isConfirming) return;
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape" && !isDeleting) closeDialog();
        };
        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [isConfirming, isDeleting, closeDialog]);

    const handleConfirm = async () => {
        if (deletingRef.current || !password.trim()) return;
        deletingRef.current = true;
        setError(null);
        setIsDeleting(true);
        try {
            await deleteStudent(userId, password);
            setPassword("");
            navigate("/admin/learners", {
                replace: true,
                state: { deleted: userName },
            });
        } catch (failure) {
            const status = errorStatus(failure);
            if (status === 400) setError(t("learnerProgress.detail.deleteIncorrectPassword"));
            else if (status === 409) setError(t("learnerProgress.detail.deletePasswordUnavailable"));
            else setError(backendMessage(failure) ?? t("learnerProgress.detail.deleteFailed"));
        } finally {
            deletingRef.current = false;
            setIsDeleting(false);
        }
    };

    return (
        <section className="danger-zone">
            <h2>{t("learnerProgress.detail.dangerZoneTitle")}</h2>
            <p>{t("learnerProgress.detail.dangerZoneText")}</p>
            <button
                type="button"
                className="btn btn-danger"
                onClick={() => { setPassword(""); setError(null); setIsConfirming(true); }}
            >
                {t("learnerProgress.detail.deleteButton")}
            </button>

            {isConfirming && (
                <div className="feedback-backdrop">
                    <div
                        className="feedback-dialog"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="delete-student-title"
                    >
                        <h2 id="delete-student-title">
                            {t("learnerProgress.detail.deleteConfirmTitle", { name: userName })}
                        </h2>
                        <form onSubmit={event => { event.preventDefault(); void handleConfirm(); }}>
                            <p>
                                {t("learnerProgress.detail.deleteConfirmText", { name: userName })}
                            </p>
                            <label htmlFor="admin-delete-password">
                                {t("learnerProgress.detail.deletePasswordLabel")}
                            </label>
                            <p>{t("learnerProgress.detail.deletePasswordHelp")}</p>
                            <input
                                id="admin-delete-password"
                                type="password"
                                autoComplete="current-password"
                                required
                                value={password}
                                onChange={event => setPassword(event.target.value)}
                                disabled={isDeleting}
                            />
                            {error && <p className="feedback-error" role="alert">{error}</p>}
                            <div className="feedback-actions">
                                <button
                                    type="button"
                                    className="btn btn-secondary"
                                    onClick={closeDialog}
                                    disabled={isDeleting}
                                >
                                    {t("learnerProgress.detail.deleteConfirmCancel")}
                                </button>
                                <button
                                    type="submit"
                                    className="btn btn-danger"
                                    disabled={isDeleting || !password.trim()}
                                >
                                    {isDeleting
                                        ? t("learnerProgress.detail.deleteConfirmDeleting")
                                        : t("learnerProgress.detail.deleteConfirmButton")}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </section>
    );
};

export default DangerZone;
