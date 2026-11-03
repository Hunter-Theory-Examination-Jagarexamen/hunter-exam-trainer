import "../../styles/exam.css";

interface ExamNavigationProps {
    onPrevious: () => void;
    onNext: () => void;
    onSubmit: () => void;
    isFirstQuestion: boolean;
    isLastQuestion: boolean;
}

const ExamNavigation = (
    { onPrevious, onNext, onSubmit, isFirstQuestion, isLastQuestion }: ExamNavigationProps
) => {

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
                onClick={onNext}
                disabled={isLastQuestion}
            >
                Next
            </button>

            <button
                className="submit-exam-button"
                onClick={onSubmit}
            >
                Submit Exam
            </button>
        </section>
    );
};

export default ExamNavigation;
