import "../../styles/result.css";

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

    return (
        <section className="result-summary">

            <div className={`result-score ${score < 80 ? "result-score--low" : ""}`}>
                <span>{score}%</span>
                <p>
                    {correctAnswers} / {totalQuestions} Correct
                </p>
            </div>

            <div className="result-details">

                <div>
                    <span>Correct Answers</span>
                    <strong>{correctAnswers}</strong>
                </div>

                <div>
                    <span>Incorrect Answers</span>
                    <strong>{incorrectAnswers}</strong>
                </div>

                <div>
                    <span>Unanswered</span>
                    <strong>{unanswered}</strong>
                </div>

            </div>

        </section>
    );
};

export default ResultSummary;