import "../../styles/practice.css";
import type { PracticeQuestion as PracticeQuestionType } from "../../types/practiceQuestions";

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

    const selectedOption = question.options.find(
        (option) => option.id === selectedAnswer
    );

    return (
        <section className="practice-question">

            <div className="practice-question-card">
                <h2>{question.question}</h2>

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