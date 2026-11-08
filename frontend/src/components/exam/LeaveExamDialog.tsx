import { useTranslation } from "react-i18next";

interface LeaveExamDialogProps {
    onConfirm: () => void;
    onCancel: () => void;
    // True while the exam is being ended on the server.
    isLeaving?: boolean;
    // Set when ending the exam failed; shown so the student can retry or stay.
    error?: string | null;
    // After a failed try: the confirm button becomes "Leave anyway" (#188).
    leaveAnyway?: boolean;
}

// Asks before leaving a running mock exam through the menu or the back
// button (#186). Confirming ends the exam on the server without scoring it.
const LeaveExamDialog = ({ onConfirm, onCancel, isLeaving = false, error, leaveAnyway = false }: LeaveExamDialogProps) => {
    const { t } = useTranslation();

    return (
        <div className="exam-leave-backdrop">
            <div className="exam-leave-dialog" role="dialog"
                 aria-modal="true" aria-labelledby="leave-exam-title">
                <h2 id="leave-exam-title">{t("exam.leave.title")}</h2>
                <p>{t("exam.leave.text")}</p>
                {error && <p role="alert" className="exam-leave-error">{error}</p>}
                <div className="exam-leave-actions">
                    <button
                        type="button"
                        className="next-button"
                        onClick={onConfirm}
                        disabled={isLeaving}
                    >
                        {leaveAnyway ? t("exam.leave.confirmAnyway") : t("exam.leave.confirm")}
                    </button>
                    <button
                        type="button"
                        className="previous-button"
                        onClick={onCancel}
                        disabled={isLeaving}
                    >
                        {t("exam.leave.stay")}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default LeaveExamDialog;
