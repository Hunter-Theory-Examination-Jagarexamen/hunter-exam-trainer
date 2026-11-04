import "../../styles/statistics.css";
import PageTitle from "../../components/common/PageTitle";
import SummaryCards from "../../components/statistics/SummaryCards";
import ProgressBar from "../../components/statistics/ProgressBar";
import RecentResults from "../../components/statistics/RecentResults";
import PerformanceSummary from "../../components/statistics/PerformanceSummary";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { RecentActivity } from "../../types/dashboardInfo.ts";
import apiClient from "../../api/apiClient.ts";
import type { SubjectPerformance } from "../../types/subjectPerformance.ts";
import ProgressOverTimeChart from "../../components/statistics/ProgressOverTimeChart";
import type { ProgressPoint } from "../../types/progressOverTime.ts";

const Statistics = () => {

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
                setRecentError("Kunde inte ladda senaste resultat. Försök igen.");

            } finally {
                setIsLoadingRecent(false);
            }
        };

        void fetchRecentResults();
    }, []);

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

                setProgressError(
                    "Kunde inte ladda ämnesprestationer. Försök igen."
                );

            } finally {
                setIsLoadingProgress(false);
            }
        };

        void fetchSubjectPerformance();
    }, []);

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
                setProgressOverTimeError(
                    "Kunde inte ladda framsteg över tid. Försök igen."
                );
            } finally {
                setIsLoadingProgressOverTime(false);
            }
        };

        void fetchProgressOverTime();
    }, [weeksRange]);

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
                title="Statistik"
                subtitle="Visa dina inlärningsframsteg."
            />
            <SummaryCards />

            <section className="dashboard-section">
                <div className="section-header">
                    <h2>Framsteg över tid</h2>
                    <select
                        value={weeksRange}
                        onChange={(e) => setWeeksRange(Number(e.target.value))}
                        className="weeks-select"
                        aria-label="Tidsintervall för framsteg över tid"
                    >
                        <option value={4}>Senaste 4 veckorna</option>
                        <option value={8}>Senaste 8 veckorna</option>
                        <option value={12}>Senaste 12 veckorna</option>
                    </select>
                </div>

                <div className="statistics-card">
                    {isLoadingProgressOverTime && <p>Laddar framsteg...</p>}

                    {!isLoadingProgressOverTime && progressOverTimeError && (
                        <p role="alert">{progressOverTimeError}</p>
                    )}

                    {!isLoadingProgressOverTime && !progressOverTimeError && (
                        <ProgressOverTimeChart data={progressOverTime} />
                    )}
                </div>
            </section>

            <section className="dashboard-section">
                <h2>Prestation per ämne</h2>
                {isLoadingProgress && (
                    <p>Laddar ämnesprestationer...</p>
                )}

                {!isLoadingProgress && progressError && (
                    <p role="alert">{progressError}</p>
                )}

                {!isLoadingProgress &&
                    !progressError &&
                    subjectPerformance.length === 0 && (
                        <p>Inga övningsresultat ännu.</p>
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

                <h2>Senaste resultat</h2>

                <div className="statistics-card">
                    {isLoadingRecent && (
                        <p>Laddar senaste resultat...</p>
                    )}

                    {recentError && (
                        <p role="alert">{recentError}</p>
                    )}

                    {!isLoadingRecent &&
                        !recentError &&
                        recentActivities.length === 0 && (
                            <p>Inga senaste resultat ännu.</p>
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
                <h2>Prestationssammanfattning</h2>

                <div className="statistics-card">
                    {subjectPerformance.length === 0 ? (
                        <p>Inga övningsresultat ännu.</p>
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
