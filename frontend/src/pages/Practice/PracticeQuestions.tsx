import {useParams} from "react-router-dom";
import PageTitle from "../../components/common/PageTitle";
import PracticeQuestion from "../../components/practice/PracticeQuestion.tsx";
import PracticeNavigation from "../../components/practice/PracticeNavigation.tsx";
import {practiceQuestions} from "../../types/practiceQuestions.ts";
import {useState} from "react";

const PracticeQuestions = () => {

    const { subject } = useParams();

    const questions = practiceQuestions.filter(
        (question) =>
            question.subjectArea.toLowerCase() === subject?.toLowerCase()
    );

    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    const [selectedAnswer, setSelectedAnswer] = useState<string | null>(null);

    const currentQuestion = questions[currentQuestionIndex];

    const [showFeedback, setShowFeedback] = useState(false);

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

            console.log("Practice completed");
        }
    };

    const handlePrevious = () => {

        if (currentQuestionIndex > 0) {

            setCurrentQuestionIndex(currentQuestionIndex - 1);
            setSelectedAnswer(null);
            setShowFeedback(false);
        }
    };

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
                title={subject ?? "Practice"}
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