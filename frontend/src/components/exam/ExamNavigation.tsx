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

            {/* Submit lives below the question (rarely used, easy to hit by
                mistake here). Only on the last question, where Next has
                nowhere to go, does it take Next's place (#188). */}
            {isLastQuestion ? (
                <button className="next-button" onClick={onSubmit}>
                    {t("exam.navigation.submit")}
                </button>
            ) : (
                <button className="next-button" onClick={onNext}>
                    {t("exam.navigation.next")}
                </button>
            )}
        </section>
    );
};

export default ExamNavigation;
