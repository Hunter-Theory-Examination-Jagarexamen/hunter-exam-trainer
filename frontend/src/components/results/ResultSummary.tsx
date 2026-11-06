import "../../styles/result.css";
import { useTranslation } from "react-i18next";

interface ResultSummaryProps {
    score: number;
    correctAnswers: number;
    incorrectAnswers: number;
    unanswered: number;
    totalQuestions: number;
}

const ResultSummary = ({
                           score,
                           correctAnswers,
                           incorrectAnswers,
                           unanswered,
                           totalQuestions
                       }: ResultSummaryProps) => {
    const { t } = useTranslation();

    return (
        <section className="result-summary">

            <div className={`result-score ${score < 80 ? "result-score--low" : ""}`}>
                <span>{score}%</span>
                <p>
                    {t("result.correctFraction", { correct: correctAnswers, total: totalQuestions })}
                </p>
            </div>

            <div className="result-details">

                <div>
                    <span>{t("result.correctAnswers")}</span>
                    <strong>{correctAnswers}</strong>
                </div>

                <div>
                    <span>{t("result.incorrectAnswers")}</span>
                    <strong>{incorrectAnswers}</strong>
                </div>

                <div>
                    <span>{t("result.unanswered")}</span>
                    <strong>{unanswered}</strong>
                </div>

            </div>

        </section>
    );
};

export default ResultSummary;
