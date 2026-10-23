import { useEffect, useState } from "react";
import PageTitle from "../../components/common/PageTitle";
import apiClient from "../../api/apiClient.ts";
import type { LearnerProgress } from "../../types/learnerProgress.ts";
import "../../styles/statistics.css";

const LearnerProgressPage = () => {
    const [learners, setLearners] = useState<LearnerProgress[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchLearners = async () => {
            try {
                setIsLoading(true);
                setError("");

                const data: LearnerProgress[] =
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

            {!isLoading && error && <p role="alert">{error}</p>}

            {!isLoading && !error && learners.length === 0 && (
                <p>No learners yet.</p>
            )}

            {!isLoading &&
                !error &&
                learners.length > 0 &&
                learners.map((learner) => (
                    <section key={learner.userId} className="dashboard-section statistics-card">
                        <h2>{learner.fullName}</h2>
                        <p className="learner-email">{learner.email}</p>

                        <p>
                            <strong>Last activity:</strong>{" "}
                            {learner.lastActivityAt
                                ? new Date(learner.lastActivityAt).toLocaleDateString("sv-SE")
                                : "Never"}
                        </p>

                        {/* Mock exam summary */}
                        <div className="mock-exam-summary">
                            {learner.mockExams.count === 0 ? (
                                <p className="mock-exam-empty">No mock exams yet.</p>
                            ) : (
                                <>
                                    <p className="mock-exam-line">
                                        <strong>Mock exams:</strong> {learner.mockExams.count} taken
                                    </p>
                                    <p className="mock-exam-line">
                                        <strong>Latest:</strong> {learner.mockExams.latestScore}%{" "}
                                        ({new Date(learner.mockExams.latestAt!).toLocaleDateString("sv-SE")})
                                        {learner.mockExams.latestPassed
                                            ? <span className="mock-exam-pass"> · Passed</span>
                                            : <span className="mock-exam-fail"> · Not passed</span>}
                                    </p>
                                    <p className="mock-exam-line">
                                        <strong>Best:</strong> {learner.mockExams.bestScore}%
                                    </p>
                                </>
                            )}
                        </div>

                        {/* Practice subjects */}
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

export default LearnerProgressPage;