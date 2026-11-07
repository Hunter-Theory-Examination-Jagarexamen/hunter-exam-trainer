import "../../styles/exam.css";
import ExamHeader from "../../components/exam/ExamHeader";
import QuestionPanel from "../../components/exam/QuestionPanel";
import ExamNavigation from "../../components/exam/ExamNavigation";
import ExamOverview from "../../components/exam/ExamOverview";
import LeaveExamDialog from "../../components/exam/LeaveExamDialog";
import { useExamBookmarks } from "../../hooks/useExamBookmarks";
import type { ExamQuestion } from "../../types/examQuestion.ts";
import { useCallback, useEffect, useRef, useState } from "react";
import { useBlocker, useNavigate } from "react-router-dom";
import apiClient, { ApiError } from "../../api/apiClient.ts";
import { errorStatus } from "../../services/apiErrors.ts";
import { useTranslation } from "react-i18next";


interface ExamStartResponse {
    attemptId: string;
    questions: ExamQuestion[];
}

const Exam = () => {

    const navigate = useNavigate();

    const [attemptId, setAttemptId] = useState<string | null>(null);
    const [questions, setQuestions] = useState<ExamQuestion[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");
    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    const [answers, setAnswers] = useState<Record<string, string>>({});
    const [timeLeft, setTimeLeft] = useState(60 * 60);
    const { t } = useTranslation();

    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [overviewOpen, setOverviewOpen] = useState(false);
    const { bookmarks, toggleBookmark, isBookmarked } = useExamBookmarks(attemptId);

    // The start request, shared by both runs of the load effect. React's
    // StrictMode (development only) runs effects twice; without this, the page
    // would start two exams, and both would try to create the same per-subject
    // totals rows on the server (#174). A ref keeps its value between the runs
    // and, unlike state, changing it doesn't redraw the page.
    const startRequest = useRef<Promise<ExamStartResponse> | null>(null);

    // True while the exam runs: from a successful start until it is submitted
    // or abandoned. A ref, not state, so blockers below see a change in the
    // same tick — submitting navigates right away, before React re-renders
    // with new state (#186).
    const examActiveRef = useRef(false);

    // Leaving the page while an exam runs (sidebar menu, browser Back, any
    // link) asks first, with "Stay" and "Leave exam" (#186).
    const blocker = useBlocker(({ currentLocation, nextLocation }) =>
        examActiveRef.current && currentLocation.pathname !== nextLocation.pathname
    );

    const [isLeaving, setIsLeaving] = useState(false);
    const [leaveError, setLeaveError] = useState<string | null>(null);
    // One abandon request at a time: a double click must not send two (#186).
    const leaveRequest = useRef(false);

    // Ends the exam on the server without scoring it, then lets the blocked
    // navigation continue. If that fails, stay on the exam and say why (#186).
    const handleLeaveConfirmed = async () => {
        if (leaveRequest.current || !attemptId) {
            return;
        }
        leaveRequest.current = true;
        setIsLeaving(true);
        setLeaveError(null);

        try {
            await apiClient(`/api/exam/${attemptId}/abandon`, { method: "POST" });
            examActiveRef.current = false;
            blocker.proceed?.();
        }
        catch (err) {
            console.error("Failed to abandon exam: ", err);
            const status = errorStatus(err);

            if (status === 409) {
                // Already ended elsewhere (e.g. submitted in another tab):
                // the exam is over, so the navigation is safe.
                examActiveRef.current = false;
                blocker.proceed?.();
                return;
            }

            if (status === 401) {
                // Session expired: apiClient is already redirecting to the
                // login page; no leave prompt during that redirect.
                examActiveRef.current = false;
                return;
            }

            setLeaveError(t("exam.leave.failed"));
        }
        finally {
            leaveRequest.current = false;
            setIsLeaving(false);
        }
    };

    const handleLeaveCancelled = () => {
        setLeaveError(null);
        blocker.reset?.();
    };

    const submitExam = useCallback(async () => {

        try {
            const result = await apiClient("/api/exam/submit", {
                method: "POST",
                // Only which exam: the answers are already saved on the server.
                body: JSON.stringify({ attemptId })
            });

            console.log("Exam result: ", result);
            // The exam is over: no leave prompts for the navigation to the
            // result page (the ref is read before React re-renders, #186).
            examActiveRef.current = false;
            navigate("/result", {
                state: result
            });
        }
        catch (err) {
            console.error("Failed to submit exam: ", err);

            if (err instanceof ApiError && err.status === 401) {
                // Session expired: apiClient is already redirecting to login.
                // The exam stays open on the server and is closed by the next
                // start, so only suppress the leave prompt here (#186).
                examActiveRef.current = false;
                return;
            }

            alert(t("exam.submitFailed"));
        }
    }, [attemptId, navigate]);


    //Load exam questions
    useEffect(() => {

        const fetchExamQuestions = async () => {

            try {
                setIsLoading(true);
                setError("");

                if (!startRequest.current) {
                    startRequest.current = apiClient("/api/exam/start", {
                        method: "POST"
                    });
                }
                const data = await startRequest.current;
                setAttemptId(data.attemptId);
                setQuestions(data.questions);
                // The exam is running: leaving the page now asks first (#186).
                examActiveRef.current = true;
            }

            catch (err: unknown) {
                console.error("Failed to load exam questions.", err);
                const errorObj = err as { status?: number; message?: string };
                if (errorObj.status === 409 || (errorObj?.message && errorObj.message.includes("Not enough questions"))) {
                    setError(errorObj.message || t("exam.startNotEnough"));
                } else {
                    setError(t("exam.startError"));
                }
            }
            finally {
                setIsLoading(false);
            }
        };

        void fetchExamQuestions();
    }, []);

    // The browser's own prompt (reload, tab close, address bar) while an exam
    // is running (#186). The listener attaches with the page itself, and the
    // handler gates on examActiveRef: only a running exam prompts — not while
    // the exam is still starting, and never after submit or abandon. Attaching
    // here (during render()'s act) instead of in an effect keyed on attemptId
    // matters: that effect can only run after the async start response
    // re-renders and React flushes its effects, which under load can happen
    // after the exam is already visible — the browser would then close the
    // tab without asking (#186 test failure).
    useEffect(() => {
        const handleBeforeUnload = (event: BeforeUnloadEvent) => {
            if (!examActiveRef.current) {
                return; // No exam running: nothing to lose.
            }
            event.preventDefault();
            // Some browsers only show the prompt when returnValue is set.
            event.returnValue = "";
        };

        window.addEventListener("beforeunload", handleBeforeUnload);
        return () => window.removeEventListener("beforeunload", handleBeforeUnload);
    }, []);

    // Timer
    useEffect(() => {
        if (timeLeft <= 0) {
            return;
        }

        const timer = setInterval(() => {
            setTimeLeft((previousTime) => Math.max(0, previousTime - 1));
        }, 1000);

        return () => clearInterval(timer);

    }, [timeLeft]);

// Auto-submit when time runs out
    useEffect(() => {
        if (timeLeft <= 0 && questions.length > 0) {
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
        if (!attemptId) {
            return;
        }

        try {
            await apiClient(`/api/exam/${attemptId}/answer`, {
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

    const handleJumpToQuestion = (index: number) => {
        setCurrentQuestionIndex(index);
        setOverviewOpen(false);
    };

    const handleToggleBookmark = () => {
        if (!currentQuestion) return;
        toggleBookmark(currentQuestion.id);
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
                <p>{t("exam.loading")}</p>
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
                <p>{t("exam.noQuestionsAvailable")}</p>
            </div>
        );
    }

    const answeredCount = Object.keys(answers).length;
    const totalQuestions = questions.length;
    const unansweredCount = totalQuestions - answeredCount;

    return (
        <div className="exam">
            <div className="exam-progress-bar text-center my-2">
                {t("exam.answeredProgress", { answered: answeredCount, total: totalQuestions })}
            </div>

            <ExamHeader
                currentQuestion={currentQuestionIndex + 1}
                totalQuestions={questions.length}
                timeLeft={timeLeft}
                onToggleOverview={() => setOverviewOpen((open) => !open)}
                overviewOpen={overviewOpen}
            />

            <div className="exam-with-overview">
                <div className="exam-main">
                    <QuestionPanel
                        question={currentQuestion}
                        selectedAnswer={answers[currentQuestion.id]}
                        onAnswerChange={handleAnswerChange}
                        isBookmarked={isBookmarked(currentQuestion.id)}
                        onToggleBookmark={handleToggleBookmark}
                    />

                    <ExamNavigation
                        onPrevious={handlePrevious}
                        onNext={handleNext}
                        onSubmit={handleSubmit}
                        isFirstQuestion={currentQuestionIndex === 0}
                        isLastQuestion={currentQuestionIndex === questions.length - 1}
                    />
                </div>

                <aside className={`exam-overview-sidebar ${overviewOpen ? "is-open" : ""}`}>
                    <ExamOverview
                        questions={questions}
                        answers={answers}
                        bookmarks={bookmarks}
                        currentIndex={currentQuestionIndex}
                        onJumpToQuestion={handleJumpToQuestion}
                    />
                </aside>
            </div>

            {overviewOpen && (
                <div
                    className="exam-overview-backdrop"
                    onClick={() => setOverviewOpen(false)}
                    aria-hidden="true"
                />
            )}


            {showConfirmModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
                    <div className="question-card text-center max-w-md w-full">
                        <h3 className="text-xl font-bold mb-3 text-[var(--color-text)]">
                            {t("exam.confirm.title")}
                        </h3>
                        {unansweredCount > 0 ? (
                            <p className="text-[var(--color-text)] opacity-80 mb-6">
                                {t("exam.confirm.unanswered", { count: unansweredCount })}
                            </p>
                        ) : (
                            <p className="text-[var(--color-text)] opacity-80 mb-6">
                                {t("exam.confirm.allAnswered")}
                            </p>
                        )}
                        <div className="flex justify-center">
                            <button
                                onClick={handleCancelSubmit}
                                className="previous-button"
                                style={{ borderRadius: "8px", padding: "0.5rem 1rem", fontSize: "0.9rem", fontWeight: 600, marginRight: "12px" }}
                            >
                                {t("exam.confirm.goBack")}
                            </button>
                            <button
                                onClick={handleConfirmSubmit}
                                className="next-button"
                                style={{ borderRadius: "8px", padding: "0.5rem 1rem", fontSize: "0.9rem", fontWeight: 600 }}
                            >
                                {unansweredCount > 0
                                    ? t("exam.confirm.submitAnyway")
                                    : t("exam.navigation.submit")}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {blocker.state === "blocked" && (
                <LeaveExamDialog
                    onConfirm={handleLeaveConfirmed}
                    onCancel={handleLeaveCancelled}
                    isLeaving={isLeaving}
                    error={leaveError}
                />
            )}

        </div>
    );
};

export default Exam;
