import Card from "../common/Card.tsx";
import "../../styles/dashboard.css"
import {useEffect, useState} from "react";
import apiClient from "../../api/apiClient.ts";
import type {DashboardInfo} from "../../types/dashboardInfo.ts";

const StatisticsCards = () => {

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
                setError("Kunde inte ladda instrumentpanelens detaljer. Försök igen.");
            }
            finally {
                setIsLoading(false);
            }
        };
        void fetchDashboardInfo();
    }, []);

    if (isLoading) {
        return <p>Laddar instrumentpanelens detaljer...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!data) {
        return <p>Instrumentpanelens detaljer är inte tillgängliga.</p>;
    }

    return (
        <div className="dashboard-cards">
            <Card title="Låtsasprov" >
                {data.mockExams}
            </Card>
            <Card title="Besvarade frågor" >
                {data.questionsAnswered}
            </Card>
            <Card title="Genomsnittspoäng" >
                {data.averageScore}%
            </Card>
            <Card title="Bästa poäng" >
                {data.bestScore}%
            </Card>
        </div>
    );
};

export default StatisticsCards;
