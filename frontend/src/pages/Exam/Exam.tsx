import "../../styles/exam.css";
import ExamHeader from "../../components/exam/ExamHeader";
import QuestionPanel from "../../components/exam/QuestionPanel";
import ExamNavigation from "../../components/exam/ExamNavigation";
import { mockQuestions } from "../../types/mockQuestions.ts";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";


const Exam = () => {

    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

    const [answers, setAnswers] = useState<Record<string, string>>({});

    const [timeLeft, setTimeLeft] = useState(60 * 60);

    const currentQuestion = mockQuestions[currentQuestionIndex];

    const navigate = useNavigate();

    useEffect(() => {

        if (timeLeft <= 0) {
            return;
        }

        const timer = setInterval(() => {
            setTimeLeft((previousTime) => previousTime - 1);
        }, 1000);

        return () => clearInterval(timer);

    }, [timeLeft]);

    const handleAnswerChange = (answerId: string) => {
        setAnswers((previousAnswers) => ({
            ...previousAnswers,
            [currentQuestion.id]: answerId
        }));
    }

    const handleNext = () => {
        if (currentQuestionIndex < mockQuestions.length - 1) {
            setCurrentQuestionIndex(currentQuestionIndex + 1);
        }
    };

    const handlePrevious = () => {
        if (currentQuestionIndex > 0) {
            setCurrentQuestionIndex(currentQuestionIndex - 1);
        }
    };

    const handleSubmit = () => {

        let correctAnswers = 0;

        mockQuestions.forEach((question) => {

            const selectedAnswer = answers[question.id];

            const correctOption = question.options.find(
                (option) => option.isCorrect
            );

            if(selectedAnswer &&
                correctOption &&
                selectedAnswer === correctOption.id
            ) {
                correctAnswers++;
            }
        });

        const totalQuestions = mockQuestions.length;

        const unanswered = totalQuestions - Object.keys(answers).length;

        const incorrectAnswers = totalQuestions - correctAnswers - unanswered;

        const score = Math.round(
            (correctAnswers / totalQuestions) * 100
        );

        navigate("/result", {
            state: {
                score,
                correctAnswers,
                incorrectAnswers,
                unanswered,
                totalQuestions
            }
        });
    };

    return (
        <div className="exam">
            <ExamHeader
                currentQuestion={currentQuestionIndex + 1}
                totalQuestions={mockQuestions.length}
                timeLeft={timeLeft}
            />

            <QuestionPanel
                question={currentQuestion}
                selectedAnswer={answers[currentQuestion.id]}
                onAnswerChange={handleAnswerChange}
            />

            <ExamNavigation
                onPrevious={handlePrevious}
                onNext={handleNext}
                onSubmit={handleSubmit}
                isFirstQuestion={currentQuestionIndex === 0}
                isLastQuestion={currentQuestionIndex === mockQuestions.length - 1}
            />
        </div>
    );
};

export default Exam;