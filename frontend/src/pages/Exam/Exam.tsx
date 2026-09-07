import "../../styles/exam.css";
import ExamHeader from "../../components/exam/ExamHeader";
import QuestionPanel from "../../components/exam/QuestionPanel";
import ExamNavigation from "../../components/exam/ExamNavigation";
import type { ExamQuestion } from "../../types/examQuestion.ts";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient from "../../api/apiClient.ts";


const Exam = () => {

    const navigate = useNavigate();

    const [questions, setQuestions] = useState<ExamQuestion[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

    const [answers, setAnswers] = useState<Record<string, string>>({});

    const [timeLeft, setTimeLeft] = useState(60 * 60);

    useEffect(() => {

        const fetchExamQuestions = async () => {

            try {
                setIsLoading(true);
                setError("");

                const data: ExamQuestion[] =
                    await apiClient("/api/exam/start", {
                        method: "POST"
                    });
                setQuestions(data);
            }
            catch (error) {
                console.error("Failed to load exam questions.");
                setError("Failed to load exam questions. Please try again.");
            }
            finally {
                setIsLoading(false);
            }
        }
        fetchExamQuestions();

    }, []);


    useEffect(() => {

        if (timeLeft <= 0) {
            return;
        }

        const timer = setInterval(() => {
            setTimeLeft(
                (previousTime) => previousTime - 1
            );
        }, 1000);

        return () => clearInterval(timer);

    }, [timeLeft]);

    const currentQuestion = questions[currentQuestionIndex];

    const handleAnswerChange = (answerId: string) => {

        setAnswers((previousAnswers) => ({
            ...previousAnswers,
            [currentQuestion.id]: answerId
        }));
    };

    const handleNext = () => {

        if (currentQuestionIndex < questions.length - 1) {
            setCurrentQuestionIndex(currentQuestionIndex + 1);
        }
    };

    const handlePrevious = () => {

        if (currentQuestionIndex > 0) {
            setCurrentQuestionIndex(currentQuestionIndex - 1);
        }
    };

    const handleSubmit = () => {

        console.log("Answer submitted: ", answers);
        navigate("/result");
    };

    if (isLoading) {

        return (
            <div className="exam">
                <p>Loading exam questions...</p>
            </div>
        );
    }

    if (error) {

        return (
            <div className="exam">
                <p>{error}</p>
            </div>
        );
    }

    if (questions.length === 0) {

        return (
            <div className="exam">
                <p>No exam questions available.</p>
            </div>
        );
    }

    return (
        <div className="exam">
            <ExamHeader
                currentQuestion={currentQuestionIndex + 1}
                totalQuestions={questions.length}
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
                isLastQuestion={currentQuestionIndex === questions.length - 1}
            />
        </div>
    );
};

export default Exam;