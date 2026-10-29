import { useBlocker, useNavigate, useParams } from "react-router-dom";
import PageTitle from "../../components/common/PageTitle";
import PracticeQuestionCard from "../../components/practice/PracticeQuestionCard";
import PracticeNavigation from "../../components/practice/PracticeNavigation";
import QuitRoundDialog from "../../components/practice/QuitRoundDialog";
import PracticeRoundResult from "../../components/practice/PracticeRoundResult";
import type { PracticeQuestion } from "../../types/practiceQuestions";
import {useEffect, useRef, useState} from "react";
import { useTranslation } from "react-i18next";
import type {
    OptionLetter,
    PracticeAnswerResponse,
    PracticeFinishResponse,
    PracticeStartResponse
} from "../../types/practiceAttempt";
import {
    answerPracticeQuestion,
    finishPracticeRound,
    startPracticeRound
} from "../../services/practiceAttemptService";

const PracticeQuestions = () => {

    const navigate = useNavigate();
    const { t } = useTranslation();
    const { subject } = useParams();

    const [attemptId, setAttemptId] = useState<number | null>(null);
    const [subjectName, setSubjectName] = useState("");
    const [questions, setQuestions] = useState<PracticeQuestion[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    // A translation key (e.g. "practice.loadFailed"), translated when drawn, so the
    // effect below doesn't depend on t().
    const [error, setError] = useState("");

    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    // The option picked on the current question, before it is checked.
    const [selectedAnswer, setSelectedAnswer] = useState<OptionLetter | null>(null);
    // The server's replies, by question index, so Previous can show them again.
    const [answers, setAnswers] = useState<Record<number, { chosen: OptionLetter; check: PracticeAnswerResponse }>>({});
    // True while an answer is on its way to the server (blocks a second click).
    const [isChecking, setIsChecking] = useState(false);
    // The server's totals once the round is finished; the page then shows the result.
    const [result, setResult] = useState<PracticeFinishResponse | null>(null);
    // True while the "Quit this round?" box is shown.
    const [isConfirmingQuit, setIsConfirmingQuit] = useState(false);
    const [isQuitting, setIsQuitting] = useState(false);

    // The start request for this page visit. Starting a round changes data on the
    // server (it closes the student's other open round), so it must be sent only
    // once. React's StrictMode runs the effect below twice in development; the
    // second run reuses this request instead of starting a second round.
    // A new visit (leaving and coming back) is a new component, so a new round.
    const startRequest = useRef<{ subject: string; promise: Promise<PracticeStartResponse> } | null>(null);

    // True while a round is running (started, not finished). A ref, not state, so
    // the blocker below sees the change at once, e.g. when quitting navigates
    // right after finishing.
    const isRoundOpen = useRef(false);

    // Leaving the page while a round is running (menu, back button, any link)
    // asks first, with the same box as the quit button.
    const blocker = useBlocker(({ currentLocation, nextLocation }) =>
        isRoundOpen.current && currentLocation.pathname !== nextLocation.pathname);

    useEffect(() => {

        // If the subject changes or the page closes before the server answers,
        // the old answer must not overwrite the new state.
        let active = true;

        const startRound = async () => {

            if (!subject) {
                setError("practice.subjectNotFound");
                setIsLoading(false);
                return;
            }

            try {

                setIsLoading(true);
                setError("");

                if (startRequest.current?.subject !== subject) {
                    startRequest.current = { subject, promise: startPracticeRound(Number(subject)) };
                }
                const round = await startRequest.current.promise;
                if (!active) {
                    return;
                }

                // Option ids are the letters A-D, the same letters the server checks.
                const mappedQuestions: PracticeQuestion[] = round.questions.map((question) => ({
                    id: question.id.toString(),
                    subjectArea: round.subjectName,
                    question: question.questionText,
                    imageUrl: question.imageUrl,
                    options: [
                        { id: "A", answer: question.optionA },
                        { id: "B", answer: question.optionB },
                        { id: "C", answer: question.optionC },
                        { id: "D", answer: question.optionD }
                    ]
                }));

                setAttemptId(round.attemptId);
                isRoundOpen.current = true;
                setSubjectName(round.subjectName);
                setQuestions(mappedQuestions);

                // Start from the first question whenever a subject is loaded
                setCurrentQuestionIndex(0);
                setSelectedAnswer(null);
                setAnswers({});
                setResult(null);
                setIsConfirmingQuit(false);
            }
            catch (error: unknown) {
                if (!active) {
                    return;
                }
                console.error("Failed to start practice round: ", error);
                const errorObj = error as { status?: number; message?: string };
                if (errorObj?.status === 409) {
                    setError("practice.noQuestionsYet");
                } else {
                    setError("practice.loadFailed");
                }
            }
            finally {
                if (active) {
                    setIsLoading(false);
                }
            }
        };
        startRound();

        return () => {
            active = false;
        };

    }, [subject]);

    const currentQuestion = questions[currentQuestionIndex];
    const currentAnswer = answers[currentQuestionIndex];

    const handleAnswerSelect = (answerId: string) => {
        // An answered question is read-only: the server keeps the first answer.
        if (currentAnswer) {
            return;
        }
        setSelectedAnswer(answerId as OptionLetter);
    }

    const handleCheckAnswer = async () => {
        if (!selectedAnswer || attemptId === null || isChecking) {
            return;
        }

        setIsChecking(true);
        try {
            const check = await answerPracticeQuestion(attemptId, Number(currentQuestion.id), selectedAnswer);
            setAnswers((previous) => ({
                ...previous,
                [currentQuestionIndex]: { chosen: selectedAnswer, check }
            }));
        } catch (error) {
            // A visible message with "Try again" comes in a later step (#170).
            console.error("Failed to save answer:", error);
        } finally {
            setIsChecking(false);
        }
    };

    const goToQuestion = (index: number) => {
        setCurrentQuestionIndex(index);
        setSelectedAnswer(null);
    };

    // Finishes the round on the server and shows its result. Returns the totals,
    // or null if it failed.
    const finishRound = async (): Promise<PracticeFinishResponse | null> => {
        if (attemptId === null) {
            return null;
        }
        try {
            const totals = await finishPracticeRound(attemptId);
            isRoundOpen.current = false;
            setResult(totals);
            return totals;
        } catch (error) {
            // A visible message with "Try again" comes in a later step (#170).
            console.error("Failed to finish practice round:", error);
            return null;
        }
    };

    const backToSubjects = () => navigate("/practice/subjects");

    const handleNext = async () => {

        if (currentQuestionIndex < questions.length - 1) {

            goToQuestion(currentQuestionIndex + 1);

        } else {

            await finishRound();
        }
    };

    const handleQuitConfirmed = async () => {
        setIsQuitting(true);
        const totals = await finishRound();
        setIsQuitting(false);
        setIsConfirmingQuit(false);
        // Nothing answered: no result to show, back to the subject list.
        if (totals && totals.answeredQuestions === 0) {
            backToSubjects();
        }
    };

    // Confirmed in the box shown when leaving through the menu or back button:
    // finish the round, then go where the user was going. The answers are already
    // saved, so the user leaves even if finishing fails (the next start closes it).
    const handleLeaveConfirmed = async () => {
        setIsQuitting(true);
        await finishRound();
        isRoundOpen.current = false;
        setIsQuitting(false);
        blocker.proceed?.();
    };

    const handlePrevious = () => {

        if (currentQuestionIndex > 0) {

            goToQuestion(currentQuestionIndex - 1);
        }
    };

    if (isLoading) {
        return (
            <div>
                <PageTitle
                    title={t("practice.title")}
                    subtitle={t("practice.loadingQuestions")}
                />
            </div>
        );
    }

    if (error) {
        return (
            <div>
                <PageTitle
                    title={t("practice.title")}
                    subtitle={t(error)}
                />
            </div>
        );
    }

    if (result) {
        return (
            <div className="practice-questions">
                <PageTitle title={subjectName} subtitle={t("practice.subtitle")} />
                <PracticeRoundResult result={result} onBack={backToSubjects} />
            </div>
        );
    }

    if (!currentQuestion) {
        return (
            <div>
                <PageTitle
                    title={t("practice.title")}
                    subtitle={t("practice.noQuestionsYet")}
                />
            </div>
        );
    }

    return (
        <div className="practice-questions">
            <PageTitle
                title={subjectName}
                subtitle={t("practice.subtitle")}
            />

            <div className="practice-quit-row">
                <button className="practice-secondary-button" onClick={() => setIsConfirmingQuit(true)}
                        disabled={isConfirmingQuit}>
                    {t("practice.quitRound")}
                </button>
            </div>

            {isConfirmingQuit && (
                <QuitRoundDialog
                    onConfirm={handleQuitConfirmed}
                    onCancel={() => setIsConfirmingQuit(false)}
                    isQuitting={isQuitting}
                />
            )}

            {blocker.state === "blocked" && (
                <QuitRoundDialog
                    onConfirm={handleLeaveConfirmed}
                    onCancel={() => blocker.reset?.()}
                    isQuitting={isQuitting}
                />
            )}

            <div className="practice-progress">
                {t("practice.progress", { current: currentQuestionIndex + 1, total: questions.length })}
            </div>

            <PracticeQuestionCard
                question={currentQuestion}
                selectedAnswer={currentAnswer?.chosen ?? selectedAnswer}
                feedback={currentAnswer?.check ?? null}
                isChecking={isChecking}
                onAnswerSelect={handleAnswerSelect}
                onCheckAnswer={handleCheckAnswer}
            />

            <PracticeNavigation
                onPrevious={handlePrevious}
                onNext={handleNext}
                isFirstQuestion={currentQuestionIndex === 0}
                isLastQuestion={currentQuestionIndex === questions.length - 1}
                canGoNext={currentAnswer !== undefined}
            />
        </div>
    );
};

export default PracticeQuestions;
