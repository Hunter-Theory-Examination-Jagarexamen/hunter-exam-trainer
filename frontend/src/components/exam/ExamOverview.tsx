import "../../styles/exam.css";
import { useTranslation } from "react-i18next";
import type { ExamQuestion } from "../../types/examQuestion";

interface ExamOverviewProps {
    questions: ExamQuestion[];
    answers: Record<string, string>;
    bookmarks: Set<number>;
    currentIndex: number;
    onJumpToQuestion: (index: number) => void;
}

const ExamOverview = ({
                          questions,
                          answers,
                          bookmarks,
                          currentIndex,
                          onJumpToQuestion,
                      }: ExamOverviewProps) => {

    const { t } = useTranslation();

    const answeredCount = Object.keys(answers).length;

    return (
        <nav
            className="exam-overview"
            aria-label={t("exam.overview.title", { answered: answeredCount, total: questions.length })}
        >
            <h2 className="exam-overview-title">
                {t("exam.overview.title", { answered: answeredCount, total: questions.length })}
            </h2>

            <ol className="exam-overview-grid">
                {questions.map((question, index) => {
                    const isCurrent = index === currentIndex;
                    const isAnswered = answers[String(question.id)] !== undefined;
                    const isBookmarked = bookmarks.has(question.id);

                    const classNames = ["exam-overview-cell"];
                    if (isCurrent) classNames.push("is-current");
                    if (isAnswered) classNames.push("is-answered");
                    if (isBookmarked) classNames.push("is-bookmarked");

                    const stateLabel = isCurrent
                        ? t("exam.overview.stateCurrent")
                        : isAnswered
                            ? t("exam.overview.stateAnswered")
                            : t("exam.overview.stateUnanswered");

                    return (
                        <li key={question.id}>
                            <button
                                type="button"
                                className={classNames.join(" ")}
                                onClick={() => onJumpToQuestion(index)}
                                aria-current={isCurrent ? "true" : undefined}
                                aria-label={
                                    t("exam.overview.ariaLabel", { number: index + 1 })
                                    + ", " + stateLabel
                                    + (isBookmarked ? ", " + t("exam.overview.stateBookmarked") : "")
                                }
                            >
                                <span className="exam-overview-number">{index + 1}</span>
                                {isAnswered && <span className="exam-overview-check" aria-hidden="true">✓</span>}
                                {isBookmarked && <span className="exam-overview-bookmark" aria-hidden="true">🔖</span>}
                            </button>
                        </li>
                    );
                })}
            </ol>
        </nav>
    );
};

export default ExamOverview;
