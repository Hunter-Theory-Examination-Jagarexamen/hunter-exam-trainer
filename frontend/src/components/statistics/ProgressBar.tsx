import "../../styles/statistics.css"
import { useTranslation } from "react-i18next";

interface ProgressBarProps {
    subject: string;
    masteredQuestions?: number;
    totalQuestions?: number;
    percentage: number;
    onClick?: () => void;
}

const ProgressBar = (
    { subject, masteredQuestions = 0, totalQuestions = 0, percentage, onClick }: ProgressBarProps) => {
    const { t } = useTranslation();

    return (
        <button
            type="button"
            className="subject-progress"
            onClick={onClick}
        >
            <div className="subject-header">
                <span>{subject}</span>
                <span>{totalQuestions === 0 ? t("statistics.noQuestions") : `${percentage}%`}</span>
            </div>
            <span>{t("statistics.masteredCount", { mastered: masteredQuestions, total: totalQuestions })}</span>
            <div className="progress-bar">
                <div className="progress-fill" style={{ width: `${percentage}%` }}>

                </div>
            </div>
        </button>
    );
};

export default ProgressBar;
