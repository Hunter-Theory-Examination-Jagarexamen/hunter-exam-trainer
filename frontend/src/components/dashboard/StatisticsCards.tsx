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
                setError("Failed to load dashboard details. Please try again");
            }
            finally {
                setIsLoading(false);
            }
        };
        void fetchDashboardInfo();
    }, []);

    if (isLoading) {
        return <p>Loading Dashboard details...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!data) {
        return <p>Dashboard details are not available.</p>;
    }

    return (
        <div className="dashboard-cards">
            <Card title="Mock Exams" >
                {data.mockExams}
            </Card>
            <Card title="Questions Answered" >
                {data.questionsAnswered}
            </Card>
            <Card title="Average Score" >
                {data.averageScore}%
            </Card>
            <Card title="Best Score" >
                {data.bestScore}%
            </Card>
        </div>
    );
};

export default StatisticsCards;