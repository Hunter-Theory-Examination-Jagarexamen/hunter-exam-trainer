import "../../styles/dashboard.css";
import {useEffect, useState} from "react";
import type { RecentActivity } from "../../types/dashboardInfo.ts";
import apiClient from "../../api/apiClient.ts";

const RecentActivity = () => {

    const [activities, setActivities] = useState<RecentActivity[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        const fetchRecentActivity = async () => {
            try {
                setIsLoading(true);
                setError("");

                const recentInfo: RecentActivity[] = await apiClient("/api/dashboard/recent");
                setActivities(recentInfo);
            }
            catch (error) {
                console.error("Failed to load recent activities:", error);
                setError("Failed to load recent activities. Please try again");
            }
            finally {
                setIsLoading(false);
            }
        };
        void fetchRecentActivity();
    }, []);

    if (isLoading) {
        return <p>Loading recent activities...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (activities.length === 0) {
        return <p>No recent activity yet.</p>;
    }

    return (
        <section className="dashboard-section">
            <h2>Recent Activity</h2>

            <div className="recent-activity">
                <div className="recent-activity-header">
                    <span>Activity</span>
                    <span>Score</span>
                    <span>Date</span>
                </div>
            </div>

            {activities.map((activity) => {

                return (
                    <div
                        className="recent-activity-row"
                        key={activity.id}
                    >
                        <span>Mock Exam</span>
                        <span>
                            {activity.score}%
                            <small>
                                {" "}({activity.correctAnswers}/{activity.totalQuestions})
                            </small>
                        </span>
                        <span>
                            {new Date(
                                activity.completedAt
                            ).toLocaleDateString("en-US", {
                                month: "short",
                                day: "numeric",
                                year: "numeric"
                            })}
                        </span>
                    </div>
                );
            })}
        </section>
    );
};

export default RecentActivity;