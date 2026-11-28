import "../../styles/statistics.css"
import { useTranslation } from "react-i18next";

interface ProgressBarProps {
    subject: string;
    mastered?: number;
    total?: number;
    percentage: number;
    onClick?: () => void;
}

const ProgressBar = (
    { subject, mastered = 0, total = 0, percentage, onClick }: ProgressBarProps) => {
    const { t } = useTranslation();

    return (
        <button
            type="button"
            className="subject-progress"
            onClick={onClick}
        >
            <div className="subject-header">
                <span>{subject}</span>
                <span>{total === 0 ? t("statistics.noQuestions") : `${percentage}%`}</span>
            </div>
            <span>{t("statistics.masteredCount", { mastered, total })}</span>
            <div className="progress-bar">
                <div className="progress-fill" style={{ width: `${percentage}%` }}>

                </div>
            </div>
        </button>
    );
};

export default ProgressBar;
