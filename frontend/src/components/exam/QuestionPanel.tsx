import "../../styles/exam.css";
import type {Question} from "../../types/mockQuestions.ts";

interface QuestionPanelProps {
    question: Question;
    selectedAnswer?: string;
    onAnswerChange: (answerId: string) => void;
}

const QuestionPanel = (
    { question, selectedAnswer, onAnswerChange }: QuestionPanelProps) => {

    return (
        <section className="question-panel">

            <div className="question-card">
                <h3>{question.question}</h3>
            </div>

            <div className="answer-options">

                {question.options.map((option) => (

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