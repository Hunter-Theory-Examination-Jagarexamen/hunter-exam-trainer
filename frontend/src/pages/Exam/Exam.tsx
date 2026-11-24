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
import { errorStatus, isServerUnreachable } from "../../services/apiErrors.ts";
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
    // Shown on the page when submitting fails, so the student can read it
    // and try again (#188). Was a browser alert() before.
    const [submitError, setSubmitError] = useState<string | null>(null);
    // Answers remain selected while pending; this message explains their state.
    const [answerSaveError, setAnswerSaveError] = useState<string | null>(null);
    const [pendingAnswers, setPendingAnswers] = useState<Record<string, string>>({});
    // True while a hand-in is on its way: the Submit buttons say "Submitting…"
    // and are disabled, so a retry is visible and can't be sent twice (#188).
    const [isSubmitting, setIsSubmitting] = useState(false);
    const submitRequest = useRef(false);
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
    // Set after ending the exam failed once: the dialog then offers "Leave
    // anyway", which leaves even if the server still can't end it (#188).
    // The exam stays open on the server and is closed by the next start.
    const [leaveFailed, setLeaveFailed] = useState(false);
    // One abandon request at a time: a double click must not send two (#186).
    const leaveRequest = useRef(false);
    const pendingRef = useRef<Record<string, { answer: string; version: number }>>({});
    const confirmedRef = useRef<Record<string, string>>({});
    const answerVersion = useRef(0);
    const syncPromise = useRef<Promise<boolean> | null>(null);
    const retryTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
    const mountedRef = useRef(true);
    const endingRef = useRef(false);
    const retryableRef = useRef(true);

    const updatePending = useCallback((questionId: number, answer?: string) => {
        const key = String(questionId);
        if (answer === undefined) delete pendingRef.current[key];
        else pendingRef.current[key] = { answer, version: answerVersion.current };
        if (mountedRef.current) setPendingAnswers(Object.fromEntries(
            Object.entries(pendingRef.current).map(([id, pending]) => [id, pending.answer])
        ));
    }, []);

    const syncPendingAnswers = useCallback((): Promise<boolean> => {
        if (syncPromise.current) return syncPromise.current;
        retryableRef.current = true;
        const run = async () => {
            while (attemptId && Object.keys(pendingRef.current).length > 0) {
                const [questionId, pending] = Object.entries(pendingRef.current)[0];
                if (confirmedRef.current[questionId] === pending.answer) {
                    if (pendingRef.current[questionId]?.version === pending.version) updatePending(Number(questionId));
                    continue;
                }
                try {
                    await apiClient(`/api/exam/${attemptId}/answer`, {
                        method: "POST",
                        body: JSON.stringify({ questionId: Number(questionId), answer: pending.answer }),
                    });
                    confirmedRef.current[questionId] = pending.answer;
                    if (pendingRef.current[questionId]?.version === pending.version) updatePending(Number(questionId));
                } catch (err) {
                    const status = errorStatus(err);
                    if (status === 401) return false;
                    // 4xx responses are permanent (invalid/expired/unauthorized); network,
                    // throttling and server errors may recover and are retried with bounds.
                    retryableRef.current = isServerUnreachable(err) || status === undefined || status >= 500 || status === 408 || status === 429;
                    return false;
                }
            }
            return Object.keys(pendingRef.current).length === 0;
        };
        syncPromise.current = run().finally(() => { syncPromise.current = null; });
        return syncPromise.current;
    }, [attemptId, updatePending]);

    const requestSync = useCallback(() => {
        if (endingRef.current || !attemptId || Object.keys(pendingRef.current).length === 0) return;
        void syncPendingAnswers().then((saved) => {
            if (saved) {
                if (retryTimer.current) clearTimeout(retryTimer.current);
                retryTimer.current = null;
                if (mountedRef.current) setAnswerSaveError(null);
            } else if (retryableRef.current && !endingRef.current && mountedRef.current && !retryTimer.current) {
                retryTimer.current = setTimeout(() => { retryTimer.current = null; requestSync(); }, 3000);
            }
        });
    }, [attemptId, syncPendingAnswers]);

    useEffect(() => {
        mountedRef.current = true;
        const online = () => {
            if (retryTimer.current) clearTimeout(retryTimer.current);
            retryTimer.current = null;
            requestSync();
        };
        window.addEventListener("online", online);
        return () => {
            mountedRef.current = false;
            window.removeEventListener("online", online);
            if (retryTimer.current) clearTimeout(retryTimer.current);
            retryTimer.current = null;
        };
    }, [requestSync]);


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

            if (leaveFailed) {
                // "Leave anyway": the student has seen the error and chose to go.
                examActiveRef.current = false;
                blocker.proceed?.();
                return;
            }

            setLeaveError(t("exam.leave.failed"));
            setLeaveFailed(true);
        }
        finally {
            leaveRequest.current = false;
            setIsLeaving(false);
        }
    };

    const handleLeaveCancelled = () => {
        setLeaveError(null);
        setLeaveFailed(false);
        blocker.reset?.();
    };

    const submitExam = useCallback(async () => {
        if (submitRequest.current) {
            return;
        }
        submitRequest.current = true;
        setIsSubmitting(true);
        setSubmitError(null);
        try {
            endingRef.current = true;
            if (retryTimer.current) clearTimeout(retryTimer.current);
            retryTimer.current = null;
            if (Object.keys(pendingRef.current).length > 0) {
                const saved = await syncPendingAnswers();
                if (!saved || Object.keys(pendingRef.current).length > 0) {
                    endingRef.current = false;
                    setAnswerSaveError(t("exam.unsavedSubmit"));
                    requestSync();
                    return;
                }
            }
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
                state: {
                    ...result,
                    // The bookmarked questions, so the result page can list them (#199).
                    bookmarkedQuestions: questions.filter(q => bookmarks.has(q.id))
                }
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

            setSubmitError(t("exam.submitFailed"));
        }
        finally {
            endingRef.current = false;
            submitRequest.current = false;
            setIsSubmitting(false);
        }
    }, [attemptId, navigate, t, questions, bookmarks, syncPendingAnswers, requestSync]);


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
    }, [t]);

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

    // The "Submit Exam?" pop-up: move focus into it when it opens, so keyboard
    // and screen reader users land in it, and let Escape close it (#188).
    const confirmDialogRef = useRef<HTMLDivElement>(null);
    useEffect(() => {
        if (!showConfirmModal) return;
        confirmDialogRef.current?.focus();
        const closeOnEscape = (e: KeyboardEvent) => {
            if (e.key === "Escape") setShowConfirmModal(false);
        };
        window.addEventListener("keydown", closeOnEscape);
        return () => window.removeEventListener("keydown", closeOnEscape);
    }, [showConfirmModal]);

    const currentQuestion = questions[currentQuestionIndex];

    // On a new question, scroll back up to it if the student had scrolled
    // past its start (long question on a phone); otherwise leave the page
    // where it is (#188).
    const questionTopRef = useRef<HTMLDivElement>(null);
    useEffect(() => {
        const questionTop = questionTopRef.current;
        if (questionTop && questionTop.getBoundingClientRect().top < 0) {
            questionTop.scrollIntoView({ block: "start" });
        }
    }, [currentQuestionIndex]);

    const handleAnswerChange = async (answerId: string) => {
        // Skip if the user re-clicked the same option (no-op on the server too).
        if (answers[currentQuestion.id] === answerId) {
            return;
        }

        const questionId = currentQuestion.id;
        answerVersion.current += 1;
        updatePending(questionId, answerId);

        // Update local state immediately for UI responsiveness.
        setAnswers((previousAnswers) => ({
            ...previousAnswers,
            [questionId]: answerId
        }));

        if (!attemptId) {
            return;
        }

        setAnswerSaveError(null);
        requestSync();
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
                <div className="exam-main" ref={questionTopRef}>
                    <QuestionPanel
                        question={currentQuestion}
                        selectedAnswer={answers[currentQuestion.id]}
                        onAnswerChange={handleAnswerChange}
                        disabled={isSubmitting}
                        isUnsaved={pendingAnswers[String(currentQuestion.id)] !== undefined}
                        isBookmarked={isBookmarked(currentQuestion.id)}
                        onToggleBookmark={handleToggleBookmark}
                    />

                    {/* Handing in early: below the answers, out of the way of
                        Previous / Next. The last question has Submit in the
                        bottom bar instead (#188). */}
                    {currentQuestionIndex < questions.length - 1 && (
                        <div className="exam-submit-early">
                            <button type="button" className="btn btn-secondary submit-exam-button"
                                    onClick={handleSubmit} disabled={isSubmitting}>
                                {isSubmitting ? t("exam.navigation.submitting") : t("exam.navigation.submit")}
                            </button>
                        </div>
                    )}

                    {/* Pinned to the bottom of the screen, so the buttons stay
                        in the same place for every question (#188). */}
                    <div className="exam-bottom-bar">
                        {answerSaveError && (
                            <p role="alert" className="exam-submit-error">{answerSaveError}</p>
                        )}
                        {Object.keys(pendingAnswers).length > 0 && (
                            <p role="status" className="exam-unsaved-status">{t("exam.unsavedCount", { count: Object.keys(pendingAnswers).length })}</p>
                        )}
                        {submitError && (
                            <p role="alert" className="exam-submit-error">{submitError}</p>
                        )}
                        <ExamNavigation
                            onPrevious={handlePrevious}
                            onNext={handleNext}
                            onSubmit={handleSubmit}
                            isFirstQuestion={currentQuestionIndex === 0}
                            isLastQuestion={currentQuestionIndex === questions.length - 1}
                            isSubmitting={isSubmitting}
                        />
                    </div>
                </div>

                <aside className={`exam-overview-sidebar ${overviewOpen ? "is-open" : ""}`}>
                    <ExamOverview
                        questions={questions}
                        answers={answers}
                        pendingAnswers={pendingAnswers}
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
                <div className="exam-leave-backdrop">
                    <div
                        ref={confirmDialogRef}
                        className="exam-leave-dialog"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="submit-exam-title"
                        tabIndex={-1}
                    >
                        <h2 id="submit-exam-title">{t("exam.confirm.title")}</h2>
                        <p>
                            {unansweredCount > 0
                                ? t("exam.confirm.unanswered", { count: unansweredCount })
                                : t("exam.confirm.allAnswered")}
                        </p>
                        <div className="exam-leave-actions">
                            <button type="button" className="btn btn-secondary" onClick={handleCancelSubmit}>
                                {t("exam.confirm.goBack")}
                            </button>
                            <button type="button" className="btn btn-primary" onClick={handleConfirmSubmit}>
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
                    leaveAnyway={leaveFailed}
                    hasUnsaved={Object.keys(pendingAnswers).length > 0}
                />
            )}

        </div>
    );
};

export default Exam;
