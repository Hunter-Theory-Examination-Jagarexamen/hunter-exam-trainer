import "../../styles/exam.css";
import { Bookmark } from "lucide-react";
import { useTranslation } from "react-i18next";
import type { ExamQuestion } from "../../types/examQuestion.ts";
import QuestionImage from "../common/QuestionImage";

interface QuestionPanelProps {
    question: ExamQuestion;
    selectedAnswer?: string;
    onAnswerChange: (answerId: string) => void;
    isBookmarked: boolean;
    onToggleBookmark: () => void;
}

const QuestionPanel = ({
                           question,
                           selectedAnswer,
                           onAnswerChange,
                           isBookmarked,
                           onToggleBookmark,
                       }: QuestionPanelProps) => {

    const { t } = useTranslation();

    const options = [
        { id: "A", text: question.optionA },
        { id: "B", text: question.optionB },
        { id: "C", text: question.optionC },
        { id: "D", text: question.optionD }
    ];

    return (
        <section className="question-panel">

            <div className="question-card">
                <div className="question-card-header">
                    <button
                        type="button"
                        className={`question-bookmark-toggle ${isBookmarked ? "is-bookmarked" : ""}`}
                        onClick={onToggleBookmark}
                        aria-pressed={isBookmarked}
                        aria-label={isBookmarked
                            ? t("exam.bookmark.ariaLabelRemove")
                            : t("exam.bookmark.ariaLabelAdd")}
                    >
                        <Bookmark
                            size={16}
                            fill={isBookmarked ? "currentColor" : "none"}
                        />
                        {isBookmarked
                            ? t("exam.bookmark.remove")
                            : t("exam.bookmark.add")}
                    </button>
                </div>
                <QuestionImage imageUrl={question.imageUrl} questionText={question.questionText} />
                <h3>{question.questionText}</h3>
            </div>

            <div className="answer-options">

                {options.map((option) => (

                    <label
                        key={option.id}
                        className={`answer-option ${
                            selectedAnswer === option.id ? "selected" : ""
                        }`}
                    >
                        <input
                            type="radio"
                            name={`question-${question.id}`}
                            value={option.id}
                            checked={selectedAnswer === option.id}
                            onChange={() => onAnswerChange(option.id)}
                        />

                        <span>{option.text}</span>

                    </label>
                ))}

            </div>

        </section>
    );
};

export default QuestionPanel;
