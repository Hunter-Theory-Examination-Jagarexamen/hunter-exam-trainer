import Card from "../common/Card";
import "../../styles/statistics.css";
import {useEffect, useState} from "react";
import type {DashboardInfo} from "../../types/dashboardInfo.ts";
import apiClient from "../../api/apiClient.ts";

const SummaryCards = () => {

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
                setError("Kunde inte ladda statistik. Försök igen.");

            } finally {
                setIsLoading(false);
            }
        };

        void fetchDashboardInfo();

    }, []);

    if (isLoading) {
        return <p>Laddar statistik...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!data) {
        return <p>Statistik är inte tillgänglig.</p>;
    }

    return (
        <div className="statistics-cards">
            <Card title="Låtsasprov">
                {data.mockExams}
            </Card>
            <Card title="Besvarade frågor">
                {data.questionsAnswered}
            </Card>
            <Card title="Genomsnittspoäng">
                {data.averageScore}%
            </Card>
            <Card title="Bästa poäng">
                {data.bestScore}%
            </Card>
        </div>
    );
};

export default SummaryCards;
