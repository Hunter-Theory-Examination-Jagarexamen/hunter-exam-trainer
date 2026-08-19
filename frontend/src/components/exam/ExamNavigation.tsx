import "../../styles/exam.css";

interface ExamNavigationProps {
    onPrevious: () => void;
    onNext: () => void;
    onSubmit: () => void;
    isFirstQuestion: boolean;
    isLastQuestion: boolean;
}

const ExamNavigation = (
    { onPrevious, onNext, onSubmit, isFirstQuestion, isLastQuestion }: ExamNavigationProps) => {

    return (
        <section className="exam-navigation">
            <button
                className="previous-button"
                onClick={onPrevious}
                disabled={isFirstQuestion}
            >
                Previous
            </button>

            <button
                className="next-button"
                onClick={isLastQuestion ? onSubmit : onNext}
            >
                {isLastQuestion ? "Submit Exam" : "Next"}
            </button>
        </section>
    );
};

export default ExamNavigation;