import { useEffect, useRef } from "react";
import "../../styles/practice.css";
import type { PracticeQuestion } from "../../types/practiceQuestions";
import type { PracticeAnswerResponse } from "../../types/practiceAttempt";
import QuestionImage from "../common/QuestionImage";

interface PracticeQuestionProps {
    question: PracticeQuestion;
    selectedAnswer: string | null;
    // The server's check of the answer; null until the answer is checked.
    feedback: PracticeAnswerResponse | null;
    onAnswerSelect: (answerId: string) => void;
    onCheckAnswer: () => void;
}

const PracticeQuestionCard = ({
                              question,
                              selectedAnswer,
                              feedback,
                              onAnswerSelect,
                              onCheckAnswer
                          }: PracticeQuestionProps) => {

    // A handle to the feedback box on the page, so we can move focus to it.
    const feedbackRef = useRef<HTMLDivElement>(null);
    const showFeedback = feedback !== null;

    // When the feedback appears, move focus to it. The Check Answer button is
    // disabled at that moment, so without this, focus falls back to the whole page
    // and screen readers announce the page title instead of the feedback.
    useEffect(() => {
        if (showFeedback) {
            feedbackRef.current?.focus();
        }
    }, [showFeedback]);

    // Which option the server says is correct (option ids are the letters A-D).
    const isCorrectOption = (optionId: string) => feedback?.correctOption === optionId;

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
                                        ? feedback.correct
                                            ? "correct"
                                            : "incorrect"
                                        : "selected"
                                    : isCorrectOption(option.id)
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

                {feedback && (
                    <div
                        ref={feedbackRef}
                        tabIndex={-1}
                        className={
                            feedback.correct
                                ? "answer-feedback correct"
                                : "answer-feedback incorrect"
                        }
                    >
                        {
                            feedback.correct ?
                                (<p>Correct!</p>):
                                (<p>Incorrect. The correct answer is: {" "}
                                    <strong>
                                        {
                                            question.options.find(
                                                (option) => isCorrectOption(option.id)
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

export default PracticeQuestionCard;
