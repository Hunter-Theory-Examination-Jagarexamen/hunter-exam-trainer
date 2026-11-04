import "../../styles/exam.css";
import type { ExamQuestion } from "../../types/examQuestion";

interface ExamOverviewProps {
    questions: ExamQuestion[];
    answers: Record<string, string>;
    bookmarks: Set<number>;
    currentIndex: number;
    onJumpToQuestion: (index: number) => void;
}

/**
 * Grid of all exam questions: one cell per question, in order.
 * <p>
 *
 * Each cell shows the question number and one or more state indicators:
 * - current: border + shadow
 * - answered: filled background + ✓
 * - bookmarked: 🔖 icon in the corner
 * - unanswered: outline only
 * <p>
 *
 * Cells are buttons, so keyboard users can tab between them.
 */
const ExamOverview = ({
                          questions,
                          answers,
                          bookmarks,
                          currentIndex,
                          onJumpToQuestion,
                      }: ExamOverviewProps) => {

    return (
        <nav
            className="exam-overview"
            aria-label="Question overview"
        >
            <h2 className="exam-overview-title">
                Overview · {Object.keys(answers).length} / {questions.length} answered
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

                    return (
                        <li key={question.id}>
                            <button
                                type="button"
                                className={classNames.join(" ")}
                                onClick={() => onJumpToQuestion(index)}
                                aria-current={isCurrent ? "true" : undefined}
                                aria-label={
                                    `Question ${index + 1}`
                                    + (isCurrent ? ", current" : "")
                                    + (isAnswered ? ", answered" : ", unanswered")
                                    + (isBookmarked ? ", bookmarked" : "")
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
