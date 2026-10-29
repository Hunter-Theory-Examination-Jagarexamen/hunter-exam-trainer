import { useTranslation } from "react-i18next";
import type { PracticeFinishResponse } from "../../types/practiceAttempt";

interface PracticeRoundResultProps {
    result: PracticeFinishResponse;
    onBack: () => void;
}

// The result after a round: "X of Y correct" when every question was answered,
// otherwise "answered X of Y, Z correct" (skipped questions don't count as wrong).
const PracticeRoundResult = ({ result, onBack }: PracticeRoundResultProps) => {
    const { t } = useTranslation();
    const { correctAnswers, answeredQuestions, totalQuestions } = result;
    const allAnswered = answeredQuestions === totalQuestions;

    return (
        <section className="practice-question-card" aria-labelledby="round-result-title">
            <h2 id="round-result-title">{t("practice.resultTitle")}</h2>
            <p>
                {allAnswered
                    ? t("practice.resultAll", { correct: correctAnswers, total: totalQuestions })
                    : t("practice.resultPartial",
                        { answered: answeredQuestions, total: totalQuestions, correct: correctAnswers })}
            </p>
            <button className="check-answer-button" onClick={onBack}>
                {t("practice.backToSubjects")}
            </button>
        </section>
    );
};

export default PracticeRoundResult;
