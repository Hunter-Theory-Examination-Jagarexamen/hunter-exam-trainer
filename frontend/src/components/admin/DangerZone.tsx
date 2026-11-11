import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { deleteStudent } from "../../services/adminLearnerService";
import { backendMessage } from "../../services/apiErrors";
import "../../styles/admin.css";

interface DangerZoneProps {
    userId: number;
    userName: string;
}

/**
 * "Delete student account" section on the student detail page (#<new-issue-number>).
 * Reuses the backend endpoint from #194. Kept in its own component so the
 * upcoming promote/demote (#204) can sit next to it.
 */
const DangerZone = ({ userId, userName }: DangerZoneProps) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [isConfirming, setIsConfirming] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Escape closes the confirmation (matches FeedbackDialog / LeaveExamDialog).
    useEffect(() => {
        if (!isConfirming) return;
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape" && !isDeleting) setIsConfirming(false);
        };
        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [isConfirming, isDeleting]);

    const handleConfirm = async () => {
        setError(null);
        setIsDeleting(true);
        try {
            await deleteStudent(userId);
            navigate("/admin/learners", {
                replace: true,
                state: { deleted: userName },
            });
        } catch (failure) {
            setError(backendMessage(failure) ?? t("learnerProgress.detail.deleteFailed"));
            setIsDeleting(false);
        }
    };

    return (
        <section className="danger-zone">
            <h2>{t("learnerProgress.detail.dangerZoneTitle")}</h2>
            <p>{t("learnerProgress.detail.dangerZoneText")}</p>
            <button
                type="button"
                className="btn-danger"
                onClick={() => setIsConfirming(true)}
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
                        <p>
                            {t("learnerProgress.detail.deleteConfirmText", { name: userName })}
                        </p>
                        {error && <p className="feedback-error" role="alert">{error}</p>}
                        <div className="feedback-actions">
                            <button
                                type="button"
                                className="practice-secondary-button"
                                onClick={() => { setIsConfirming(false); setError(null); }}
                                disabled={isDeleting}
                            >
                                {t("learnerProgress.detail.deleteConfirmCancel")}
                            </button>
                            <button
                                type="button"
                                className="btn-danger"
                                onClick={handleConfirm}
                                disabled={isDeleting}
                            >
                                {isDeleting
                                    ? t("learnerProgress.detail.deleteConfirmDeleting")
                                    : t("learnerProgress.detail.deleteConfirmButton")}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </section>
    );
};

export default DangerZone;
