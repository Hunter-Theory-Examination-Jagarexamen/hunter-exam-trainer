import { useNavigate, useParams } from "react-router-dom";
import PageTitle from "../../components/common/PageTitle";
import PracticeQuestion from "../../components/practice/PracticeQuestion";
import PracticeNavigation from "../../components/practice/PracticeNavigation";
import type { PracticeQuestion as PracticeQuestionType } from "../../types/practiceQuestions";
import {useEffect, useState} from "react";
import type {Question} from "../../types/question.ts";
import apiClient from "../../api/apiClient.ts";

const PracticeQuestions = () => {

    const navigate = useNavigate();
    const { subject } = useParams();

    const [questions, setQuestions] = useState<PracticeQuestionType[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);
    const [showFeedback, setShowFeedback] = useState(false);

    useEffect(() => {

        const fetchQuestions = async () => {

            if (!subject) {
                setError("Subject not found");
                setIsLoading(false);
                return;
            }

            try {

                setIsLoading(true);
                setError("");

                const data: Question[]= await apiClient(`/api/questions?subjectId=${subject}`);

                const mappedQuestions: PracticeQuestionType[] =
                    data.map((question) => {

                        return {
                            id: question.id.toString(),
                            subjectArea: question.subject.name,
                            question: question.questionText,

                            options: [
                                {
                                    id: "A",
                                    answer: question.optionA,
                                    isCorrect: question.optionA === question.correctAnswer
                                },
                                {
                                    id: "B",
                                    answer: question.optionB,
                                    isCorrect: question.optionB === question.correctAnswer
                                },
                                {
                                    id: "C",
                                    answer: question.optionC,
                                    isCorrect: question.optionC === question.correctAnswer
                                },
                                {
                                    id: "D",
                                    answer: question.optionD,
                                    isCorrect: question.optionD === question.correctAnswer
                                }
                            ]
                        };
                });
                setQuestions(mappedQuestions);

                // Start from the first question whenever a subject is loaded
                setCurrentQuestionIndex(0);
                setSelectedAnswer(null);
                setShowFeedback(false);
            }
            catch (error) {
                console.error("Failed to fetch questions: ", error);
                setError("Failed to load questions. Please try again");
            }
            finally {
                setIsLoading(false);
            }
        };
        fetchQuestions();

    }, [subject]);

    const currentQuestion = questions[currentQuestionIndex];

    const handleAnswerSelect = (answerId: string) => {
        setSelectedAnswer(answerId);
    }

    const handleCheckAnswer = () => {
        if (selectedAnswer) {
            setShowFeedback(true);
        }
    };

    const handleNext = () => {

        if (currentQuestionIndex < questions.length - 1) {

            setCurrentQuestionIndex(currentQuestionIndex + 1);
            setSelectedAnswer(null);
            setShowFeedback(false);

        } else {

            navigate("/practice/subjects");
        }
    };

    const handlePrevious = () => {

        if (currentQuestionIndex > 0) {

            setCurrentQuestionIndex(currentQuestionIndex - 1);
            setSelectedAnswer(null);
            setShowFeedback(false);
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
                title={currentQuestion.subjectArea}
                subtitle="Answer the questions and improve your knowledge."
            />

            <div className="practice-progress">
                Question {currentQuestionIndex + 1} of {questions.length}
            </div>

            <PracticeQuestion
                question={currentQuestion}
                selectedAnswer={selectedAnswer}
                showFeedback={showFeedback}
                onAnswerSelect={handleAnswerSelect}
                onCheckAnswer={handleCheckAnswer}
            />

            <PracticeNavigation
                onPrevious={handlePrevious}
                onNext={handleNext}
                isFirstQuestion={currentQuestionIndex === 0}
                isLastQuestion={currentQuestionIndex === questions.length - 1}
                canGoNext={showFeedback}
            />
        </div>
    );
};

export default PracticeQuestions;