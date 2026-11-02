import "../../styles/exam.css";
import ExamHeader from "../../components/exam/ExamHeader";
import QuestionPanel from "../../components/exam/QuestionPanel";
import ExamNavigation from "../../components/exam/ExamNavigation";
import type { ExamQuestion } from "../../types/examQuestion.ts";
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiClient, { ApiError } from "../../api/apiClient.ts";


const Exam = () => {

    const navigate = useNavigate();

    const [sessionId, setSessionId] = useState<string | null>(null);
    const [questions, setQuestions] = useState<ExamQuestion[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");
    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    const [answers, setAnswers] = useState<Record<string, string>>({});
    const [timeLeft, setTimeLeft] = useState(60 * 60);

    const [showConfirmModal, setShowConfirmModal] = useState(false);

    const submitExam = useCallback(async () => {

        try {
            const result = await apiClient("/api/exam/submit", {
                method: "POST",
                body: JSON.stringify({
                    sessionId,
                    answers
                })
            });

            console.log("Exam result: ", result);
            navigate("/result", {
                state: result
            });
        }
        catch (err) {
            console.error("Failed to submit exam: ", err);

            if (err instanceof ApiError && err.status === 401) {
                return;
            }

            alert(
                "Failed to submit exam. Your answers are saved on the server — please try again."
            );
        }
    }, [sessionId, answers, navigate]);


    //Load exam questions
    useEffect(() => {

        const fetchExamQuestions = async () => {

            try {
                setIsLoading(true);
                setError("");

                interface ExamStartResponse {
                    sessionId: string;
                    questions: ExamQuestion[];
                }

                const data: ExamStartResponse =
                    await apiClient("/api/exam/start", {
                        method: "POST"
                    });
                setSessionId(data.sessionId);
                setQuestions(data.questions);
            }

            catch (err: unknown) {
                console.error("Failed to load exam questions.", err);
                const errorObj = err as { status?: number; message?: string };
                if (errorObj.status === 409 || (errorObj?.message && errorObj.message.includes("Not enough questions"))) {
                    setError(errorObj.message || "Not enough questions available to start the exam.");
                } else {
                    setError("Failed to load exam questions. Please try again.");
                }
            }
            finally {
                setIsLoading(false);
            }
        };

        void fetchExamQuestions();
    }, []);

    // Timer
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

    // Auto-submit when time runs out
    useEffect(() => {
        if (timeLeft === 0 && questions.length > 0) {
            void submitExam();
        }
    }, [timeLeft, submitExam, questions]);

    const handleSubmit = () => {
        setShowConfirmModal(true);
    }

    const handleConfirmSubmit = () => {
        setShowConfirmModal(false);
        void submitExam();
    };

    const handleCancelSubmit = () => {
        setShowConfirmModal(false);
    }

    const currentQuestion = questions[currentQuestionIndex];

    const handleAnswerChange = async (answerId: string) => {
        // Skip if the user re-clicked the same option (no-op on the server too).
        if (answers[currentQuestion.id] === answerId) {
            return;
        }

        // Update local state immediately for UI responsiveness.
        setAnswers((previousAnswers) => ({
            ...previousAnswers,
            [currentQuestion.id]: answerId
        }));

        // Persist to the server (best-effort). If this fails, we keep going:

        // the local state still holds the answer, and the user can try again by selecting a different option (which retries the save).
        if (!sessionId) {
            return;
        }

        try {
            await apiClient(`/api/exam/${sessionId}/answer`, {
                method: "POST",
                body: JSON.stringify({
                    questionId: currentQuestion.id,
                    answer: answerId,
                }),
            });
        }
        catch (err) {
            // Do not interrupt the user mid-exam.  Log for debugging.
            console.error("Failed to save answer:", err);

        }
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

    const answeredCount = Object.keys(answers).length;
    const totalQuestions = questions.length;
    const unansweredCount = totalQuestions - answeredCount;

    return (
        <div className="exam">

            <div className="exam-progress-bar text-center my-2">
                <span className="font-semibold text-gray-700">
                    Answered {answeredCount} / {totalQuestions}
                </span>
            </div>

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

            {showConfirmModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
                    <div className="question-card text-center max-w-md w-full">
                        <h3 className="text-xl font-bold mb-3 text-[var(--color-text)]">Submit Exam?</h3>
                        {unansweredCount > 0 ? (
                            <p className="text-[var(--color-text)] opacity-80 mb-6">
                                You have <strong>{unansweredCount}</strong> unanswered question{unansweredCount > 1 ? "s" : ""}. Are you sure you want to submit?
                            </p>
                        ) : (
                            <p className="text-[var(--color-text)] opacity-80 mb-6">
                                You have answered all questions. Are you sure you want to submit?
                            </p>
                        )}
                        <div className="flex justify-center">
                            <button
                                onClick={handleCancelSubmit}
                                className="previous-button"
                                style={{ borderRadius: "8px", padding: "0.5rem 1rem", fontSize: "0.9rem", fontWeight: 600, marginRight: "12px" }}
                            >
                                Go back
                            </button>
                            <button
                                onClick={handleConfirmSubmit}
                                className="next-button"
                                style={{ borderRadius: "8px", padding: "0.5rem 1rem", fontSize: "0.9rem", fontWeight: 600 }}
                            >
                                Submit Exam
                            </button>
                        </div>
                    </div>
                </div>
            )}

        </div>
    );
};

export default Exam;
