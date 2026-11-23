import { useTranslation } from "react-i18next";
import type { LearnerProgress } from "../../types/learnerProgress";
import "../../styles/statistics.css";

// One user's progress: last activity, mock exam summary and results per subject.
// Shown on the admin's detail page for a user.
// Sharing (#125): a null category means the student has not shared it, so it is
// labelled as such instead of being shown as "no results".
const LearnerProgressCard = ({ learner }: { learner: LearnerProgress }) => {
    const { t } = useTranslation();

    const mockExams = learner.mockExams;
    const subjectPerformances = learner.subjectPerformances;
    const nothingShared = mockExams === null && subjectPerformances === null;
    const partlyHidden = mockExams === null || subjectPerformances === null;

    if (nothingShared) {
        return (
            <section className="dashboard-section statistics-card">
                <p>{t("learnerProgress.nothingShared")}</p>
            </section>
        );
    }

    return (
        <section className="dashboard-section statistics-card">
            <p>
                <strong>{t("learnerProgress.lastActivity")}</strong>{" "}
                {learner.lastActivityAt
                    ? new Date(learner.lastActivityAt).toLocaleDateString("sv-SE")
                    : partlyHidden
                        ? t("learnerProgress.noSharedActivity")
                        : t("learnerProgress.never")}
            </p>

            {/* Mock exam summary */}
            <div className="mock-exam-summary">
                {mockExams === null ? (
                    <p className="mock-exam-empty">{t("learnerProgress.notShared")}</p>
                ) : mockExams.count === 0 ? (
                    <p className="mock-exam-empty">{t("learnerProgress.noMockExams")}</p>
                ) : (
                    <>
                        <p className="mock-exam-line">
                            <strong>{t("learnerProgress.mockExamsCount")}</strong> {mockExams.count} {t("learnerProgress.taken")}
                        </p>
                        <p className="mock-exam-line">
                            <strong>{t("learnerProgress.latest")}</strong> {mockExams.latestScore}%{" "}
                            ({new Date(mockExams.latestAt!).toLocaleDateString("sv-SE")})
                            {mockExams.latestPassed
                                ? <span className="mock-exam-pass">{t("learnerProgress.passed")}</span>
                                : <span className="mock-exam-fail">{t("learnerProgress.notPassed")}</span>}
                        </p>
                        <p className="mock-exam-line">
                            <strong>{t("learnerProgress.best")}</strong> {mockExams.bestScore}%
                        </p>
                    </>
                )}
            </div>

            {/* Practice subjects */}
            {subjectPerformances === null ? (
                <p>{t("learnerProgress.notShared")}</p>
            ) : subjectPerformances.length === 0 ? (
                <p>{t("learnerProgress.noPracticeResults")}</p>
            ) : (
                <div className="subject-performance-grid">
                    {subjectPerformances.map((subject) => (
                        <div
                            key={subject.subjectId}
                            className="subject-performance-item"
                        >
                            <div className="subject-performance-header">
                                <span>{subject.subjectName}</span>
                                <span>{subject.totalQuestions === 0 ? t("statistics.noQuestions") : `${subject.masteryPercentage}%`}</span>
                            </div>
                            <p>{t("statistics.masteredCount", { mastered: subject.masteredQuestions, total: subject.totalQuestions })}</p>
                            <div className="progress-bar-track">
                                <div
                                    className="progress-bar-fill"
                                    style={{ width: `${subject.masteryPercentage}%` }}
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
