import {Clock} from "lucide-react";
import "../../styles/exam.css";

interface ExamHeaderProps {
    currentQuestion: number;
    totalQuestions: number;
    timeLeft: number;
}

const ExamHeader = (
    { currentQuestion, totalQuestions, timeLeft } : ExamHeaderProps) => {

    const progress = (currentQuestion / totalQuestions) * 100;

    const minutes = Math.floor(timeLeft / 60);

    const seconds = timeLeft % 60;

    const formattedTime =
        `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;

    return (
        <section className="exam-header">
            <div className="exam-header-top">
                <h2>
                    Question {currentQuestion} of {totalQuestions}
                </h2>
                <span className="timer">
                    <Clock size={16} />
                    {formattedTime}
                </span>
            </div>

            <div className="exam-progress">
                <div
                    className="progress-fill"
                    style={{ width: `${progress}%` }}
                />
            </div>
        </section>
    );
};

export default ExamHeader;