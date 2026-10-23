import { useEffect, useRef } from "react";
import "../../styles/practice.css";
import type { PracticeQuestion as PracticeQuestionType } from "../../types/practiceQuestions";
import QuestionImage from "../common/QuestionImage";

interface PracticeQuestionProps {
    question: PracticeQuestionType;
    selectedAnswer: string | null;
    showFeedback: boolean;
    onAnswerSelect: (answerId: string) => void;
    onCheckAnswer: () => void;
}

const PracticeQuestion = ({
                              question,
                              selectedAnswer,
                              showFeedback,
                              onAnswerSelect,
                              onCheckAnswer
                          }: PracticeQuestionProps) => {

    // A handle to the feedback box on the page, so we can move focus to it.
    const feedbackRef = useRef<HTMLDivElement>(null);

    // When the feedback appears, move focus to it. The Check Answer button is
    // disabled at that moment, so without this, focus falls back to the whole page
    // and screen readers announce the page title instead of the feedback.
    useEffect(() => {
        if (showFeedback) {
            feedbackRef.current?.focus();
        }
    }, [showFeedback]);

    const selectedOption = question.options.find(
        (option) => option.id === selectedAnswer
    );

    return (
        <section className="practice-question">

            <div className="practice-question-card">
                <h2>{question.question}</h2>
                <QuestionImage imageUrl={question.imageUrl} questionText={question.question} />

                <div className="practice-answer-options">
                    {question.options.map((option) => (

                        <label
                            key={option.id}
                            className={`practice-answer ${
                                selectedAnswer === option.id 
                                    ? showFeedback 
                                        ? option.isCorrect 
                                            ? "correct" 
                                            : "incorrect"
                                        : "selected"
                                    : showFeedback && option.isCorrect 
                                        ? "correct" 
                                        : ""
                            }`}
                        >
                            <input
                                type="radio"
                                name="practice-answer"
                                value={option.id}
                                checked={selectedAnswer === option.id}
                                onChange={() => onAnswerSelect(option.id)}
                            />
                            <span>{option.answer}</span>
                        </label>

                    ))}
                </div>

                <button
                    className="check-answer-button"
                    onClick={onCheckAnswer}
                    disabled={!selectedAnswer || showFeedback}
                >
                    Check Answer
                </button>

                {showFeedback && selectedOption && (
                    <div
                        ref={feedbackRef}
                        tabIndex={-1}
                        className={
                            selectedOption.isCorrect
                                ? "answer-feedback correct"
                                : "answer-feedback incorrect"
                        }
                    >
                        {
                            selectedOption.isCorrect ?
                                (<p>Correct!</p>):
                                (<p>Incorrect. The correct answer is: {" "}
                                    <strong>
                                        {
                                            question.options.find(
                                                (option) => option.isCorrect
                                            )?.answer
                                        }
                                    </strong>
                                </p>)
                        }
                    </div>
                )}

            </div>

        </section>
    );
};

export default PracticeQuestion;
