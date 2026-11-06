import PageTitle from "../../components/common/PageTitle";
import ResultSummary from "../../components/results/ResultSummary";
import "../../styles/result.css";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";

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
        </section>
    );
};

export default Result;
