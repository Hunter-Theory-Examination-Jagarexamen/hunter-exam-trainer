import "../../styles/practice.css";
import { useTranslation } from "react-i18next";

interface PracticeNavigationProps {
    onPrevious: () => void;
    onNext: () => void;
    isFirstQuestion: boolean;
    isLastQuestion: boolean;
    canGoNext: boolean;
}

const PracticeNavigation = (
    { onPrevious, onNext, isFirstQuestion, isLastQuestion, canGoNext }: PracticeNavigationProps) => {

    const { t } = useTranslation();

    return (
        <div className="practice-navigation">

            <button
                onClick={onPrevious}
                disabled={isFirstQuestion}
            >
                {t("practice.previous")}
            </button>

            <button
                onClick={onNext}
                disabled={!canGoNext}
            >
                {isLastQuestion ? t("practice.finish") : t("practice.next")}
            </button>
        </div>
    );
};

export default PracticeNavigation;