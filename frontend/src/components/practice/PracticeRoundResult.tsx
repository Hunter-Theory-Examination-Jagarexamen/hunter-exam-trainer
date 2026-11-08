import { useTranslation } from "react-i18next";
import type { PracticeFinishResponse } from "../../types/practiceAttempt";

interface PracticeRoundResultProps {
    result: PracticeFinishResponse;
    onBack: () => void;
    // Translation key for the back button: the subject list by default, or
    // the practice mode cards after a random round.
    backLabelKey?: string;
}

// The result after a round: "X of Y correct" when every question was answered,
// otherwise "answered X of Y, Z correct" (skipped questions don't count as wrong).
const PracticeRoundResult = ({ result, onBack, backLabelKey = "practice.backToSubjects" }: PracticeRoundResultProps) => {
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
                {t(backLabelKey)}
            </button>
        </section>
    );
};

export default PracticeRoundResult;
