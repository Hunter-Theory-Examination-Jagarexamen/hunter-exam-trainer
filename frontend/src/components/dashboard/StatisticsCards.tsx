import Card from "../common/Card.tsx";
import "../../styles/dashboard.css";
import {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import apiClient from "../../api/apiClient.ts";
import type {DashboardInfo} from "../../types/dashboardInfo.ts";

const StatisticsCards = () => {
    const {t} = useTranslation();
    const [data, setData] = useState<DashboardInfo | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchDashboardInfo = async () => {
            try {
                setIsLoading(true);
                setError("");

                const dashboardData: DashboardInfo = await apiClient("/api/dashboard");
                setData(dashboardData);
            }
            catch (error) {
                console.error("Failed to load dashboard details:", error);
                setError(t("dashboard.detailsError"));
            }
            finally {
                setIsLoading(false);
            }
        };
        void fetchDashboardInfo();
    }, [t]);

    if (isLoading) {
        return <p>{t("dashboard.loadingDetails")}</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!data) {
        return <p>{t("dashboard.noDetails")}</p>;
    }

    return (
        <div className="dashboard-cards">
            <Card title={t("dashboard.cardMockExams")}>
                {data.mockExams}
            </Card>
            <Card title={t("dashboard.cardQuestionsAnswered")}>
                {data.questionsAnswered}
            </Card>
            <Card title={t("dashboard.cardAverageScore")}>
                {data.averageScore}%
            </Card>
            <Card title={t("dashboard.cardBestScore")}>
                {data.bestScore}%
            </Card>
        </div>
    );
};

export default StatisticsCards;
