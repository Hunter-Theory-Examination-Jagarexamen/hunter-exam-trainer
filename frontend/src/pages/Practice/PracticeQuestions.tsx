import { useNavigate, useParams } from "react-router-dom";
import PageTitle from "../../components/common/PageTitle";
import PracticeQuestionCard from "../../components/practice/PracticeQuestionCard";
import PracticeNavigation from "../../components/practice/PracticeNavigation";
import type { PracticeQuestion } from "../../types/practiceQuestions";
import {useEffect, useState} from "react";
import type {OptionLetter, PracticeAnswerResponse} from "../../types/practiceAttempt";
import {
    answerPracticeQuestion,
    finishPracticeRound,
    startPracticeRound
} from "../../services/practiceAttemptService";

const PracticeQuestions = () => {

    const navigate = useNavigate();
    const { subject } = useParams();

    const [attemptId, setAttemptId] = useState<number | null>(null);
    const [subjectName, setSubjectName] = useState("");
    const [questions, setQuestions] = useState<PracticeQuestion[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    // The option picked on the current question, before it is checked.
    const [selectedAnswer, setSelectedAnswer] = useState<OptionLetter | null>(null);
    // The server's replies, by question index, so Previous can show them again.
    const [answers, setAnswers] = useState<Record<number, { chosen: OptionLetter; check: PracticeAnswerResponse }>>({});
    // True while an answer is on its way to the server (blocks a second click).
    const [isChecking, setIsChecking] = useState(false);

    useEffect(() => {

        // If the subject changes or the page closes before the server answers,
        // the old answer must not overwrite the new state.
        let active = true;

        const startRound = async () => {

            if (!subject) {
                setError("Subject not found");
                setIsLoading(false);
                return;
            }

            try {

                setIsLoading(true);
                setError("");

                const round = await startPracticeRound(Number(subject));
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
                setSubjectName(round.subjectName);
                setQuestions(mappedQuestions);

                // Start from the first question whenever a subject is loaded
                setCurrentQuestionIndex(0);
                setSelectedAnswer(null);
                setAnswers({});
            }
            catch (error: unknown) {
                if (!active) {
                    return;
                }
                console.error("Failed to start practice round: ", error);
                const errorObj = error as { status?: number; message?: string };
                if (errorObj?.status === 409) {
                    setError("There are no questions in this subject yet.");
                } else {
                    setError("Failed to load questions. Please try again.");
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

    const handleNext = async () => {

        if (currentQuestionIndex < questions.length - 1) {

            goToQuestion(currentQuestionIndex + 1);

        } else {

            if (attemptId === null) {
                return;
            }

            try {

                await finishPracticeRound(attemptId);

                navigate("/practice/subjects");

            } catch (error) {

                // A visible message with "Try again" comes in a later step (#170).
                console.error("Failed to finish practice round:", error);
            }
        }
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
                    title="Practice"
                    subtitle="Loading questions..."
                />
            </div>
        );
    }

    if (error) {
        return (
            <div>
                <PageTitle
                    title="Practice"
                    subtitle={error}
                />
            </div>
        );
    }

    if (!currentQuestion) {
        return (
            <div>
                <PageTitle
                    title="Practice"
                    subtitle="No questions available for this subject."
                />
            </div>
        );
    }

    return (
        <div className="practice-questions">
            <PageTitle
                title={subjectName}
                subtitle="Answer the questions and improve your knowledge."
            />

            <div className="practice-progress">
                Question {currentQuestionIndex + 1} of {questions.length}
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
