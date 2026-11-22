import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { updateUserRole } from "../../services/adminLearnerService";
import { backendMessage } from "../../services/apiErrors";
import "../../styles/admin.css";
import "../../styles/feedback.css";

type Role = "STUDENT" | "ADMIN";

interface RoleChangeSectionProps {
    userId: number;
    userName: string;
    role: Role;
    /** True when the signed-in administrator is looking at their own account. */
    isSelf: boolean;
    onRoleChanged: (role: Role) => void;
}

/**
 * "Make admin" / "Make student" on the participant detail page (#204).
 * <p>
 * The action always asks for confirmation first (the dialog explains that
 * administrators can see participants' information and results), and the
 * request is only sent from that dialog. While the request is pending both
 * buttons are disabled, so a double click cannot send it twice. An
 * administrator's own account cannot be demoted here: the button is disabled
 * with an explanation, and the backend refuses it as well (409).
 */
const RoleChangeSection = ({
    userId,
    userName,
    role,
    isSelf,
    onRoleChanged,
}: RoleChangeSectionProps) => {
    const { t } = useTranslation();
    const promoting = role === "STUDENT";
    const selfDemotionBlocked = isSelf && !promoting;

    const [isConfirming, setIsConfirming] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    // Escape closes the confirmation (matches FeedbackDialog / DangerZone).
    useEffect(() => {
        if (!isConfirming) return;
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape" && !isSaving) setIsConfirming(false);
        };
        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [isConfirming, isSaving]);

    const handleConfirm = async () => {
        if (isSaving) return;
        setError(null);
        setIsSaving(true);
        try {
            const nextRole: Role = promoting ? "ADMIN" : "STUDENT";
            await updateUserRole(userId, nextRole);
            setSuccess(t(
                promoting
                    ? "learnerProgress.detail.promoteSuccess"
                    : "learnerProgress.detail.demoteSuccess",
                { name: userName }
            ));
            setIsConfirming(false);
            // The page shows the new role right away; the list re-reads its
            // roles (admins first) the next time it is opened.
            onRoleChanged(nextRole);
        } catch (failure) {
            setError(backendMessage(failure)
                ?? t("learnerProgress.detail.roleChangeFailed"));
        } finally {
            setIsSaving(false);
        }
    };

    return (
        <section className="role-change">
            <h2>{t("learnerProgress.detail.roleChangeTitle")}</h2>

            {success && <p className="success-banner" role="status">{success}</p>}

            <button
                type="button"
                className="role-change-button"
                onClick={() => { setError(null); setIsConfirming(true); }}
                disabled={isSaving || selfDemotionBlocked}
            >
                {promoting
                    ? t("learnerProgress.detail.promoteButton")
                    : t("learnerProgress.detail.demoteButton")}
            </button>

            {selfDemotionBlocked && (
                <p className="role-change-note">
                    {t("learnerProgress.detail.selfDemotionBlocked")}
                </p>
            )}

            {isConfirming && (
                <div className="feedback-backdrop">
                    <div
                        className="feedback-dialog"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="role-change-title"
                    >
                        <h2 id="role-change-title">
                            {t(
                                promoting
                                    ? "learnerProgress.detail.promoteConfirmTitle"
                                    : "learnerProgress.detail.demoteConfirmTitle",
                                { name: userName }
                            )}
                        </h2>
                        <p>
                            {t(
                                promoting
                                    ? "learnerProgress.detail.promoteConfirmText"
                                    : "learnerProgress.detail.demoteConfirmText",
                                { name: userName }
                            )}
                        </p>
                        {error && <p className="feedback-error" role="alert">{error}</p>}
                        <div className="feedback-actions">
                            <button
                                type="button"
                                className="practice-secondary-button"
                                onClick={() => { setIsConfirming(false); setError(null); }}
                                disabled={isSaving}
                            >
                                {t("learnerProgress.detail.roleConfirmCancel")}
                            </button>
                            <button
                                type="button"
                                className="role-change-button"
                                onClick={handleConfirm}
                                disabled={isSaving}
                            >
                                {isSaving
                                    ? t("learnerProgress.detail.roleSaving")
                                    : t(
                                        promoting
                                            ? "learnerProgress.detail.promoteConfirmButton"
                                            : "learnerProgress.detail.demoteConfirmButton"
                                    )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </section>
    );
};

export default RoleChangeSection;
