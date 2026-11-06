import Card from "../common/Card";
import "../../styles/statistics.css";
import {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import type {DashboardInfo} from "../../types/dashboardInfo.ts";
import apiClient from "../../api/apiClient.ts";

const SummaryCards = () => {
    const { t } = useTranslation();
    const [data, setData] = useState<DashboardInfo | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchDashboardInfo = async () => {
            try {
                setIsLoading(true);
                setError("");

                const dashboardData: DashboardInfo =
                    await apiClient("/api/dashboard");

                setData(dashboardData);

            } catch (error) {
                console.error("Failed to load statistics:", error);
                setError(t("statistics.errorSummary"));

            } finally {
                setIsLoading(false);
            }
        };

        void fetchDashboardInfo();

    }, [t]);

    if (isLoading) {
        return <p>{t("statistics.loadingSummary")}</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!data) {
        return <p>{t("statistics.summaryNotAvailable")}</p>;
    }

    return (
        <div className="statistics-cards">
            <Card title={t("statistics.cardMockExams")}>
                {data.mockExams}
            </Card>
            <Card title={t("statistics.cardQuestionsAnswered")}>
                {data.questionsAnswered}
            </Card>
            <Card title={t("statistics.cardAverageScore")}>
                {data.averageScore}%
            </Card>
            <Card title={t("statistics.cardBestScore")}>
                {data.bestScore}%
            </Card>
        </div>
    );
};

export default SummaryCards;
