import PageTitle from "../../components/common/PageTitle";
import ResultSummary from "../../components/results/ResultSummary";
import "../../styles/result.css";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useState } from "react";
import FeedbackDialog from "../../components/feedback/FeedbackDialog";

interface ResultData {
    score: number;
    correctAnswers: number;
    incorrectAnswers: number;
    unanswered: number;
    totalQuestions: number;
}

const Result = () => {
    const { t } = useTranslation();
    const location = useLocation();
    // True while the feedback dialog is open.
    const [isGivingFeedback, setIsGivingFeedback] = useState(false);

    const result = location.state as ResultData | undefined;

    if(!result) {
        return (
            <section className="result">

                <PageTitle
                    title={t("result.title")}
                    subtitle={t("result.noDataSubtitle")}
                />
                <p>{t("result.noDataMessage")}</p>
            </section>
        );
    }

    return (
        <section className="result">

            <PageTitle
                title={t("result.title")}
                subtitle={t("result.hasDataSubtitle")}
            />

            <ResultSummary
                score={result.score}
                correctAnswers={result.correctAnswers}
                unanswered={result.unanswered}
                incorrectAnswers={result.incorrectAnswers}
                totalQuestions={result.totalQuestions}
            />

            {/* Only after the exam, never during it; no specific question is attached. */}
            <div className="result-feedback">
                <p>{t("feedback.afterExamText")}</p>
                <button type="button" className="practice-secondary-button"
                        onClick={() => setIsGivingFeedback(true)}>
                    {t("feedback.afterExamButton")}
                </button>
            </div>

            {isGivingFeedback && <FeedbackDialog onClose={() => setIsGivingFeedback(false)} />}
        </section>
    );
};

export default Result;
