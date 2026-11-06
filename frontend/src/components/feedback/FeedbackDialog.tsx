import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { sendFeedback, sendFeedbackErrorKey } from "../../services/feedbackService";
import { FEEDBACK_MAX_LENGTH, type FeedbackType } from "../../types/feedback";
import "../../styles/feedback.css";

interface FeedbackDialogProps {
    onClose: () => void;
    // Preset type; "QUESTION" when opened from a question.
    initialType?: FeedbackType;
    // Set when reporting a specific question, so the student doesn't have to describe which.
    questionId?: number;
}

const TYPES: FeedbackType[] = ["QUESTION", "APP", "OTHER"];

// A form for sending feedback or reporting a problem with a question. Shown on top
// of the page (a modal); closes with the close button or the Escape key.
const FeedbackDialog = ({ onClose, initialType = "APP", questionId }: FeedbackDialogProps) => {
    const { t } = useTranslation();
    const [type, setType] = useState<FeedbackType>(initialType);
    const [text, setText] = useState("");
    const [status, setStatus] = useState<"editing" | "sending" | "sent">("editing");
    const [errorKey, setErrorKey] = useState<string | null>(null);

    // Escape closes the dialog. The listener is removed again when the dialog closes.
    useEffect(() => {
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") onClose();
        };
        window.addEventListener("keydown", onKeyDown);
        return () => window.removeEventListener("keydown", onKeyDown);
    }, [onClose]);

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        setStatus("sending");
        setErrorKey(null);
        try {
            // The question only goes along when the report is still about it.
            await sendFeedback({
                type,
                text: text.trim(),
                ...(questionId !== undefined && type === "QUESTION" ? { questionId } : {}),
            });
            setStatus("sent");
        } catch (error) {
            setErrorKey(sendFeedbackErrorKey(error));
            setStatus("editing");
        }
    };

    return (
        <div className="feedback-backdrop">
            <div className="feedback-dialog" role="dialog" aria-modal="true" aria-labelledby="feedback-title">
                <h2 id="feedback-title">{t("feedback.title")}</h2>

                {status === "sent" ? (
                    <>
                        <p role="status">{t("feedback.sent")}</p>
                        <div className="feedback-actions">
                            <button type="button" className="check-answer-button" onClick={onClose}>
                                {t("feedback.close")}
                            </button>
                        </div>
                    </>
                ) : (
                    <form onSubmit={handleSubmit}>
                        <label htmlFor="feedback-type">{t("feedback.typeLabel")}</label>
                        <select id="feedback-type" value={type}
                                onChange={event => setType(event.target.value as FeedbackType)}>
                            {TYPES.map(option => (
                                <option key={option} value={option}>{t(`feedback.types.${option}`)}</option>
                            ))}
                        </select>

                        <label htmlFor="feedback-text">{t("feedback.textLabel")}</label>
                        <textarea id="feedback-text" value={text} autoFocus rows={6}
                                  maxLength={FEEDBACK_MAX_LENGTH}
                                  aria-describedby="feedback-hint feedback-count"
                                  onChange={event => setText(event.target.value)} />
                        <div className="feedback-meta">
                            <span id="feedback-hint">{t("feedback.privacyHint")}</span>
                            <span id="feedback-count">{text.length} / {FEEDBACK_MAX_LENGTH}</span>
                        </div>

                        {errorKey && <p className="feedback-error" role="alert">{t(errorKey)}</p>}

                        <div className="feedback-actions">
                            <button type="button" className="practice-secondary-button" onClick={onClose}
                                    disabled={status === "sending"}>
                                {t("feedback.cancel")}
                            </button>
                            <button type="submit" className="check-answer-button"
                                    disabled={status === "sending" || text.trim() === ""}>
                                {status === "sending" ? t("feedback.sending") : t("feedback.send")}
                            </button>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
};

export default FeedbackDialog;
