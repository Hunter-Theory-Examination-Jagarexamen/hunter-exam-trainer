import "../../styles/dashboard.css";
import {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import type { RecentActivity } from "../../types/dashboardInfo.ts";
import apiClient from "../../api/apiClient.ts";

const RecentActivityList = () => {
    const {t} = useTranslation();
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
                setError(t("dashboard.loadActivityError"));
            }
            finally {
                setIsLoading(false);
            }
        };
        void fetchRecentActivity();
    }, [t]);

    if (isLoading) {
        return <p>{t("dashboard.loadingActivities")}</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (activities.length === 0) {
        return <p>{t("dashboard.noActivities")}</p>;
    }

    return (
        <section className="dashboard-section">
            <h2>{t("dashboard.recentActivityTitle")}</h2>

            <div className="recent-activity">
                <div className="recent-activity-header">
                    <span>{t("dashboard.activityHeader")}</span>
                    <span>{t("dashboard.scoreHeader")}</span>
                    <span>{t("dashboard.dateHeader")}</span>
                </div>
            </div>

            {activities.map((activity) => {
                return (
                    <div
                        className="recent-activity-row"
                        key={activity.id}
                    >
                        <span>{t("dashboard.practiceActivityLabel")}</span>
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
