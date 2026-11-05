import { useEffect, useState } from "react";
import PageTitle from "../../components/common/PageTitle";
import apiClient from "../../api/apiClient.ts";
import type { LearnerProgress } from "../../types/learnerProgress.ts";
import "../../styles/statistics.css";
import { useTranslation } from "react-i18next";

const LearnerProgressPage = () => {
    const { t } = useTranslation();
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
                setError(t("learnerProgress.loadFailed"));
            } finally {
                setIsLoading(false);
            }
        };

        void fetchLearners();
    }, []);

    return (
        <div className="statistics">
            <PageTitle
                title={t("learnerProgress.title")}
                subtitle={t("learnerProgress.subtitle")}
            />

            {isLoading && <p>{t("learnerProgress.loading")}</p>}

            {!isLoading && error && <p role="alert">{error}</p>}

            {!isLoading && !error && learners.length === 0 && (
                <p>{t("learnerProgress.noLearners")}</p>
            )}

            {!isLoading &&
                !error &&
                learners.length > 0 &&
                learners.map((learner) => (
                    <section key={learner.userId} className="dashboard-section statistics-card">
                        <h2>{learner.fullName}</h2>
                        <p className="learner-email">{learner.email}</p>

                        <p>
                            <strong>{t("learnerProgress.lastActivity")}</strong>{" "}
                            {learner.lastActivityAt
                                ? new Date(learner.lastActivityAt).toLocaleDateString("sv-SE")
                                : t("learnerProgress.never")}
                        </p>

                        {/* Mock exam summary */}
                        <div className="mock-exam-summary">
                            {learner.mockExams.count === 0 ? (
                                <p className="mock-exam-empty">{t("learnerProgress.noMockExams")}</p>
                            ) : (
                                <>
                                    <p className="mock-exam-line">
                                        <strong>{t("learnerProgress.mockExamsCount")}</strong> {learner.mockExams.count} {t("learnerProgress.taken")}
                                    </p>
                                    <p className="mock-exam-line">
                                        <strong>{t("learnerProgress.latest")}</strong> {learner.mockExams.latestScore}%{" "}
                                        ({new Date(learner.mockExams.latestAt!).toLocaleDateString("sv-SE")})
                                        {learner.mockExams.latestPassed
                                            ? <span className="mock-exam-pass">{t("learnerProgress.passed")}</span>
                                            : <span className="mock-exam-fail">{t("learnerProgress.notPassed")}</span>}
                                    </p>
                                    <p className="mock-exam-line">
                                        <strong>{t("learnerProgress.best")}</strong> {learner.mockExams.bestScore}%
                                    </p>
                                </>
                            )}
                        </div>

                        {/* Practice subjects */}
                        {learner.subjectPerformances.length === 0 ? (
                            <p>{t("learnerProgress.noPracticeResults")}</p>
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
