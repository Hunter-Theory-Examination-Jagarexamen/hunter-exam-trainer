import { Clock, List } from "lucide-react";
import { useTranslation } from "react-i18next";
import "../../styles/exam.css";

interface ExamHeaderProps {
    currentQuestion: number;
    totalQuestions: number;
    timeLeft: number;
    onToggleOverview: () => void;
    overviewOpen: boolean;
}

const ExamHeader = (
    { currentQuestion, totalQuestions, timeLeft, onToggleOverview, overviewOpen } : ExamHeaderProps) => {

    const { t } = useTranslation();

    const progress = (currentQuestion / totalQuestions) * 100;
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    const formattedTime = `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;

    return (
        <section className="exam-header">
            <div className="exam-header-top">
                <h2>
                    {t("exam.questionOf", { current: currentQuestion, total: totalQuestions })}
                </h2>
                <div className="exam-header-actions">
                    <button
                        type="button"
                        className="exam-overview-toggle"
                        onClick={onToggleOverview}
                        aria-expanded={overviewOpen}
                        aria-label={t("exam.toggleOverview")}
                    >
                        <List size={16} />
                        {t("exam.toggleOverview")}
                    </button>
                    <span className="timer">
                        <Clock size={16} />
                        {formattedTime}
                    </span>
                </div>
            </div>

            <div className="exam-progress">
                <div
                    className="progress-fill"
                    style={{ width: `${progress}%` }}
                />
            </div>
        </section>
    );
};

export default ExamHeader;
