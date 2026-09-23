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
                setError("Failed to load statistics. Please try again.");

            } finally {
                setIsLoading(false);
            }
        };

        void fetchDashboardInfo();

    }, []);

    if (isLoading) {
        return <p>Loading statistics...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!data) {
        return <p>Statistics are not available.</p>;
    }

    return (
        <div className="statistics-cards">
            <Card title="Mock Exams">
                {data.mockExams}
            </Card>
            <Card title="Questions Answered">
                {data.questionsAnswered}
            </Card>
            <Card title="Avg Score">
                {data.averageScore}%
            </Card>
            <Card title="Best Score">
                {data.bestScore}%
            </Card>
        </div>
    );
};

export default SummaryCards;