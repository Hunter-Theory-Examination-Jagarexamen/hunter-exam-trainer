import { useEffect, useState } from "react";
import PageTitle from "../../components/common/PageTitle";
import apiClient from "../../api/apiClient.ts";
import type { LearnerProgress as LearnerProgressType } from "../../types/learnerProgress.ts";
import "../../styles/statistics.css";

const LearnerProgress = () => {
    const [learners, setLearners] = useState<LearnerProgressType[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchLearners = async () => {
            try {
                setIsLoading(true);
                setError("");

                const data: LearnerProgressType[] =
                    await apiClient("/api/admin/learners");

                setLearners(data);
            } catch (e) {
                console.error("Failed to load learner progress:", e);
                setError("Failed to load learner progress. Please try again.");
            } finally {
                setIsLoading(false);
            }
        };

        void fetchLearners();
    }, []);

    return (
        <div className="statistics">
            <PageTitle
                title="Learner Progress"
                subtitle="Monitor how learners are progressing."
            />

            {isLoading && <p>Loading learners...</p>}

            {!isLoading && error && <p>{error}</p>}

            {!isLoading && !error && learners.length === 0 && (
                <p>No learners yet.</p>
            )}

            {!isLoading &&
                !error &&
                learners.length > 0 &&
                learners.map((learner) => (
                    <section
                        key={learner.userId}
                        className="dashboard-section statistics-card"
                    >
                        <h2>{learner.fullName}</h2>
                        <p className="learner-email">{learner.email}</p>

                        <p>
                            <strong>Last practiced:</strong>{" "}
                            {learner.lastPracticedAt
                                ? new Date(learner.lastPracticedAt).toLocaleDateString("sv-SE")
                                : "Never"}
                        </p>

                        {learner.subjectPerformances.length === 0 ? (
                            <p>No practice results yet.</p>
                        ) : (
                            <div className="subject-performance-grid">
                                {learner.subjectPerformances.map((subject) => (
                                    <div
                                        key={subject.subjectId}
                                        className="subject-performance-item"
                                    >
                                        <div className="subject-performance-header">
                                            <span>{subject.subjectName}</span>
                                            <span>{subject.percentage}%</span>
                                        </div>
                                        <div className="progress-bar-track">
                                            <div
                                                className="progress-bar-fill"
                                                style={{ width: `${subject.percentage}%` }}
                                            />
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </section>
                ))}
        </div>
    );
};

export default LearnerProgress;