import "../../styles/dashboard.css";
import {useEffect, useState} from "react";
import type { RecentActivity } from "../../types/dashboardInfo.ts";
import apiClient from "../../api/apiClient.ts";

const RecentActivityList = () => {

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
                setError("Kunde inte ladda senaste aktiviteter. Försök igen.");
            }
            finally {
                setIsLoading(false);
            }
        };
        void fetchRecentActivity();
    }, []);

    if (isLoading) {
        return <p>Laddar senaste aktiviteter...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (activities.length === 0) {
        return <p>Ingen aktivitet än.</p>;
    }

    return (
        <section className="dashboard-section">
            <h2>Senaste aktivitet</h2>

            <div className="recent-activity">
                <div className="recent-activity-header">
                    <span>Aktivitet</span>
                    <span>Poäng</span>
                    <span>Datum</span>
                </div>
            </div>

            {activities.map((activity) => {

                return (
                    <div
                        className="recent-activity-row"
                        key={activity.id}
                    >
                        <span>Övningsprov</span>
                        <span>
                            {activity.score}%
                            <small>
                                {" "}({activity.correctAnswers}/{activity.totalQuestions})
                            </small>
                        </span>
                        <span>
                            {new Date(
                                activity.completedAt
                            ).toLocaleDateString("sv-SE", {
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

export default RecentActivityList;
