import PageTitle from "../../components/common/PageTitle";
import ResultSummary from "../../components/results/ResultSummary";
import "../../styles/result.css";
import { useLocation } from "react-router-dom";

interface ResultData {
    score: number;
    correctAnswers: number;
    incorrectAnswers: number;
    unanswered: number;
    totalQuestions: number;
}

const Result = () => {

    const location = useLocation();

    const result = location.state as ResultData | undefined;

    if(!result) {
        return (
            <section className="result">

                <PageTitle
                    title="Result"
                    subtitle="No result data found."
                />
                <p>Please complete a mock exam to see your results.</p>
            </section>
        );
    }

    return (
        <section className="result">

            <PageTitle
                title="Result"
                subtitle="Here is your result for the mock exam."
            />

            <ResultSummary
                score={result.score}
                correctAnswers={result.correctAnswers}
                unanswered={result.unanswered}
                incorrectAnswers={result.incorrectAnswers}
                totalQuestions={result.totalQuestions}
            />
        </section>
    );
};

export default Result;