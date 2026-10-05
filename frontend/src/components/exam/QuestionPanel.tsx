import "../../styles/exam.css";
import type { ExamQuestion } from "../../types/examQuestion.ts";

interface QuestionPanelProps {
    question: ExamQuestion;
    selectedAnswer?: string;
    onAnswerChange: (answerId: string) => void;
}

const QuestionPanel = ({
                           question,
                           selectedAnswer,
                           onAnswerChange
}: QuestionPanelProps) => {

    const options = [
        { id: "A", text: question.optionA },
        { id: "B", text: question.optionB },
        { id: "C", text: question.optionC },
        { id: "D", text: question.optionD }
    ];

    return (
        <section className="question-panel">

            <div className="question-card">
                {question.imageUrl && (
                    <img
                    src={question.imageUrl}
                    alt="Question visual representation"
                    style={{ maxWidth: '100%', height: 'auto', borderRadius: '8px', marginBottom: '1rem' }}
                    />
                )}
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