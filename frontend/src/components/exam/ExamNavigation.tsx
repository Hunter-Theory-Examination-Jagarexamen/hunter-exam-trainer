import "../../styles/exam.css";
import { useTranslation } from "react-i18next";

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
    const { t } = useTranslation();

    return (
        <section className="exam-navigation">
            <button
                className="previous-button"
                onClick={onPrevious}
                disabled={isFirstQuestion}
            >
                {t("exam.navigation.previous")}
            </button>

            <button
                className="next-button"
                onClick={onNext}
                disabled={isLastQuestion}
            >
                {t("exam.navigation.next")}
            </button>

            <button
                className="submit-exam-button"
                onClick={onSubmit}
            >
                {t("exam.navigation.submit")}
            </button>
        </section>
    );
};

export default ExamNavigation;
