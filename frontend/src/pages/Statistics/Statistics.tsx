import "../../styles/statistics.css";
import PageTitle from "../../components/common/PageTitle";
import SummaryCards from "../../components/statistics/SummaryCards";
import ProgressBar from "../../components/statistics/ProgressBar";
import RecentResults from "../../components/statistics/RecentResults";
import PerformanceSummary from "../../components/statistics/PerformanceSummary";
import {useEffect, useState} from "react";
import type {RecentActivity} from "../../types/dashboardInfo.ts";
import apiClient from "../../api/apiClient.ts";

const Statistics = () => {

    const [recentActivities, setRecentActivities] = useState<RecentActivity[]>([]);

    const [isLoadingRecent, setIsLoadingRecent] = useState(true);

    const [recentError, setRecentError] = useState("");

    useEffect(() => {
        const fetchRecentResults = async () => {
            try {
                setIsLoadingRecent(true);
                setRecentError("");

                const data: RecentActivity[] = await apiClient("/api/dashboard/recent");

                setRecentActivities(data);

            } catch (error) {
                console.error("Failed to load recent results:", error);
                setRecentError("Failed to load recent results. Please try again.");

            } finally {
                setIsLoadingRecent(false);
            }
        };

        void fetchRecentResults();
    }, []);

    return (
        <div className="statistics">
            <PageTitle
                title="Statistics"
                subtitle="View your learning progress."
            />
            <SummaryCards />

            <section className="dashboard-section">
                <h2>Performance by Subject</h2>
                <ProgressBar subject="Hunting Laws" percentage={65}/>
                <ProgressBar subject="Weapons & Equipment" percentage={74}/>
                <ProgressBar subject="Dog Handling" percentage={58}/>
            </section>

            <section className="dashboard-section">

                <h2>Recent Results</h2>

                <div className="statistics-card">
                    {isLoadingRecent && (
                        <p>Loading recent results...</p>
                    )}

                    {recentError && (
                        <p>{recentError}</p>
                    )}

                    {!isLoadingRecent &&
                        !recentError &&
                        recentActivities.length === 0 && (
                            <p>No recent results yet.</p>
                        )}

                    {!isLoadingRecent &&
                        !recentError &&
                        recentActivities.map((activity) => (
                            <RecentResults
                                key={activity.id}
                                activity={activity}
                            />
                        ))}
                </div>
            </section>

            <section className="dashboard-section">
                <h2>Performance Summary</h2>
                <div className="statistics-card">
                    <PerformanceSummary
                        strongestSubject="Safety"
                        needsImprovement="Dog Handling"
                    />
                </div>
            </section>
        </div>
    );
};

export default Statistics;