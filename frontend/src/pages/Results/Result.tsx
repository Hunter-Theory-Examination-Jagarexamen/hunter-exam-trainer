import PageTitle from "../../components/common/PageTitle";
import ResultSummary from "../../components/results/ResultSummary";
import "../../styles/result.css";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useState } from "react";
import FeedbackDialog from "../../components/feedback/FeedbackDialog";
import type { ExamQuestion } from "../../types/examQuestion.ts";

interface ResultData {
    score: number;
    correctAnswers: number;
    incorrectAnswers: number;
    unanswered: number;
    totalQuestions: number;
    passed: boolean;
    bookmarkedQuestions?: ExamQuestion[];
}

const Result = () => {
    const { t } = useTranslation();
    const location = useLocation();
    const [isGivingFeedback, setIsGivingFeedback] = useState(false);
    const [reportedQuestionId, setReportedQuestionId] = useState<number | null>(null);

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
                passed={result.passed}
            />

            <div className="result-feedback">
                <p>{t("feedback.afterExamText")}</p>
                <button type="button" className="practice-secondary-button"
                        onClick={() => setIsGivingFeedback(true)}>
                    {t("feedback.afterExamButton")}
                </button>
            </div>

            {result.bookmarkedQuestions && result.bookmarkedQuestions.length > 0 && (
                <>
                    <h2>{t("result.bookmarkedTitle")}</h2>
                    <ul className="result-bookmarked">
                        {result.bookmarkedQuestions.map(question => (
                            <li key={question.id}>
                                <span>{question.questionText}</span>
                                <button type="button" className="practice-secondary-button"
                                        onClick={() => setReportedQuestionId(question.id)}>
                                    {t("feedback.reportQuestion")}
                                </button>
                            </li>
                        ))}
                    </ul>
                </>
            )}

            {reportedQuestionId !== null && (
                <FeedbackDialog
                    initialType="QUESTION"
                    questionId={reportedQuestionId}
                    onClose={() => setReportedQuestionId(null)}
                />
            )}

            {isGivingFeedback && <FeedbackDialog onClose={() => setIsGivingFeedback(false)} />}
        </section>
    );
};

export default Result;
