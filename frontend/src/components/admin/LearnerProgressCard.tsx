import { useTranslation } from "react-i18next";
import type { LearnerProgress } from "../../types/learnerProgress";
import "../../styles/statistics.css";

// One user's progress: last activity, mock exam summary and results per subject.
// Shown on the admin's detail page for a user.
const LearnerProgressCard = ({ learner }: { learner: LearnerProgress }) => {
    const { t } = useTranslation();

    return (
        <section className="dashboard-section statistics-card">
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
    );
};

export default LearnerProgressCard;
