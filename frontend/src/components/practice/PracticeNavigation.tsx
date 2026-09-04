import "../../styles/practice.css";

interface PracticeNavigationProps {
    onPrevious: () => void;
    onNext: () => void;
    isFirstQuestion: boolean;
    isLastQuestion: boolean;
    canGoNext: boolean;
}

const PracticeNavigation = (
    { onPrevious, onNext, isFirstQuestion, isLastQuestion, canGoNext }: PracticeNavigationProps) => {

    return (
        <div className="practice-navigation">

            <button
                onClick={onPrevious}
                disabled={isFirstQuestion}
            >
                Previous
            </button>

            <button
                onClick={onNext}
                disabled={!canGoNext}
            >
                {isLastQuestion ? "Finish" : "Next"}
            </button>
        </div>
    );
};

export default PracticeNavigation;