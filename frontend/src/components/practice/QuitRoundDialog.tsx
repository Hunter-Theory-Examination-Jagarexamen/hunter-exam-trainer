import { useTranslation } from "react-i18next";

interface QuitRoundDialogProps {
    onConfirm: () => void;
    onCancel: () => void;
    // True while the round is being finished on the server.
    isQuitting?: boolean;
}

// Asks before leaving a practice round. Used by the page's quit button and,
// in the next step, when the user leaves through the menu or the back button.
const QuitRoundDialog = ({ onConfirm, onCancel, isQuitting = false }: QuitRoundDialogProps) => {
    const { t } = useTranslation();

    return (
        <div className="practice-question-card practice-quit-dialog" role="dialog"
             aria-modal="true" aria-labelledby="quit-round-title">
            <h2 id="quit-round-title">{t("practice.quitTitle")}</h2>
            <p>{t("practice.quitText")}</p>
            <div className="practice-dialog-actions">
                <button className="check-answer-button" onClick={onConfirm} disabled={isQuitting}>
                    {t("practice.quitConfirm")}
                </button>
                <button className="practice-secondary-button" onClick={onCancel} disabled={isQuitting}>
                    {t("practice.quitCancel")}
                </button>
            </div>
        </div>
    );
};

export default QuitRoundDialog;
