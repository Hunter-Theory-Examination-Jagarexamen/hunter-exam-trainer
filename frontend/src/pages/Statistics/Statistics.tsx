import "../../styles/statistics.css";
import PageTitle from "../../components/common/PageTitle";
import SummaryCards from "../../components/statistics/SummaryCards";
import ProgressBar from "../../components/statistics/ProgressBar";
import RecentResults from "../../components/statistics/RecentResults";
import PerformanceSummary from "../../components/statistics/PerformanceSummary";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import type { RecentActivity } from "../../types/dashboardInfo.ts";
import apiClient from "../../api/apiClient.ts";
import type { SubjectPerformance } from "../../types/subjectPerformance.ts";
import ProgressOverTimeChart from "../../components/statistics/ProgressOverTimeChart";
import type { ProgressPoint } from "../../types/progressOverTime.ts";

const Statistics = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const [recentActivities, setRecentActivities] = useState<RecentActivity[]>([]);
    const [isLoadingRecent, setIsLoadingRecent] = useState(true);
    const [recentError, setRecentError] = useState("");

    const [subjectPerformance, setSubjectPerformance] = useState<SubjectPerformance[]>([]);
    const [isLoadingProgress, setIsLoadingProgress] = useState(true);
    const [progressError, setProgressError] = useState("");

    const [progressOverTime, setProgressOverTime] = useState<ProgressPoint[]>([]);
    const [isLoadingProgressOverTime, setIsLoadingProgressOverTime] = useState(true);
    const [progressOverTimeError, setProgressOverTimeError] = useState("");
    const [weeksRange, setWeeksRange] = useState(8);

    useEffect(() => {
        const fetchRecentResults = async () => {
            try {
                setIsLoadingRecent(true);
                setRecentError("");

                const data: RecentActivity[] = await apiClient("/api/dashboard/recent");

                setRecentActivities(data);

            } catch (error) {
                console.error("Failed to load recent results:", error);
                setRecentError(t("statistics.errorRecent"));

            } finally {
                setIsLoadingRecent(false);
            }
        };

        void fetchRecentResults();
    }, [t]);

    useEffect(() => {
        const fetchSubjectPerformance = async () => {
            try {
                setIsLoadingProgress(true);
                setProgressError("");

                const data: SubjectPerformance[] =
                    await apiClient("/api/statistics/subjects");

                setSubjectPerformance(data);

            } catch (error) {
                console.error("Failed to load subject performance:", error);
                setProgressError(t("statistics.errorSubject"));

            } finally {
                setIsLoadingProgress(false);
            }
        };

        void fetchSubjectPerformance();
    }, [t]);

    useEffect(() => {
        const fetchProgressOverTime = async () => {
            try {
                setIsLoadingProgressOverTime(true);
                setProgressOverTimeError("");

                const data: ProgressPoint[] = await apiClient(
                    `/api/statistics/progress?weeks=${weeksRange}`
                );

                setProgressOverTime(data);
            } catch (error) {
                console.error("Failed to load progress over time:", error);
                setProgressOverTimeError(t("statistics.errorProgress"));
            } finally {
                setIsLoadingProgressOverTime(false);
            }
        };

        void fetchProgressOverTime();
    }, [weeksRange, t]);

    const strongestSubject =
        subjectPerformance.length > 0
            ? subjectPerformance.reduce((strongest, current) =>
                current.percentage > strongest.percentage
                    ? current
                    : strongest
            )
            : null;

    const needsImprovement =
        subjectPerformance.length > 0
            ? subjectPerformance.reduce((lowest, current) =>
                current.percentage < lowest.percentage
                    ? current
                    : lowest
            )
            : null;

    return (
        <div className="statistics">
            <PageTitle
                title={t("statistics.title")}
                subtitle={t("statistics.subtitle")}
            />
            <SummaryCards />

            <section className="dashboard-section">
                <div className="section-header">
                    <h2>{t("statistics.progressOverTimeTitle")}</h2>
                    <select
                        value={weeksRange}
                        onChange={(e) => setWeeksRange(Number(e.target.value))}
                        className="weeks-select"
                        aria-label={t("statistics.weeksRangeAria")}
                    >
                        <option value={4}>{t("statistics.last4Weeks")}</option>
                        <option value={8}>{t("statistics.last8Weeks")}</option>
                        <option value={12}>{t("statistics.last12Weeks")}</option>
                    </select>
                </div>

                <div className="statistics-card">
                    {isLoadingProgressOverTime && <p>{t("statistics.loadingProgress")}</p>}

                    {!isLoadingProgressOverTime && progressOverTimeError && (
                        <p role="alert">{progressOverTimeError}</p>
                    )}

                    {!isLoadingProgressOverTime && !progressOverTimeError && (
                        <ProgressOverTimeChart data={progressOverTime} />
                    )}
                </div>
            </section>

            <section className="dashboard-section">
                <h2>{t("statistics.subjectPerformanceTitle")}</h2>
                {isLoadingProgress && (
                    <p>{t("statistics.loadingSubjectPerformance")}</p>
                )}

                {!isLoadingProgress && progressError && (
                    <p role="alert">{progressError}</p>
                )}

                {!isLoadingProgress &&
                    !progressError &&
                    subjectPerformance.length === 0 && (
                        <p>{t("statistics.noSubjectPerformance")}</p>
                    )}

                {!isLoadingProgress &&
                    !progressError &&
                    subjectPerformance.length > 0 && (
                        <div className="subject-performance-grid">
                            {subjectPerformance.map((subject) => (
                                <ProgressBar
                                    key={subject.subjectId}
                                    subject={subject.subjectName}
                                    percentage={subject.percentage}
                                    onClick={() =>
                                        navigate(`/practice/subjects/${subject.subjectId}`)
                                    }
                                />
                            ))}
                        </div>
                    )}
            </section>

            <section className="dashboard-section">
                <h2>{t("statistics.recentResultsTitle")}</h2>

                <div className="statistics-card">
                    {isLoadingRecent && (
                        <p>{t("statistics.loadingRecentResults")}</p>
                    )}

                    {recentError && (
                        <p role="alert">{recentError}</p>
                    )}

                    {!isLoadingRecent &&
                        !recentError &&
                        recentActivities.length === 0 && (
                            <p>{t("statistics.noRecentResults")}</p>
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
                <h2>{t("statistics.performanceSummaryTitle")}</h2>

                <div className="statistics-card">
                    {subjectPerformance.length === 0 ? (
                        <p>{t("statistics.noSubjectPerformance")}</p>
                    ) : (
                        <PerformanceSummary
                            strongestSubject={strongestSubject?.subjectName ?? ""}
                            needsImprovement={needsImprovement?.subjectName ?? ""}
                        />
                    )}
                </div>
            </section>
        </div>
    );
};

export default Statistics;
