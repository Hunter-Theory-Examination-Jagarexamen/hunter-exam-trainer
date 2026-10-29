import { useTranslation } from "react-i18next";

interface PracticeErrorMessageProps {
    message: string;
    // Shown as "Försök igen" when trying again can help.
    onRetry?: () => void;
    // Shown as "Tillbaka till ämnen" when it can't (e.g. the round is closed).
    onBack?: () => void;
}

// A visible error with what the user can do next. role="alert" makes screen
// readers read it out as soon as it appears.
const PracticeErrorMessage = ({ message, onRetry, onBack }: PracticeErrorMessageProps) => {
    const { t } = useTranslation();

    return (
        <div className="answer-feedback incorrect" role="alert">
            <p>{message}</p>
            <div className="practice-dialog-actions">
                {onRetry && (
                    <button className="check-answer-button" onClick={onRetry}>{t("practice.tryAgain")}</button>
                )}
                {onBack && (
                    <button className="practice-secondary-button" onClick={onBack}>{t("practice.backToSubjects")}</button>
                )}
            </div>
        </div>
    );
};

export default PracticeErrorMessage;
