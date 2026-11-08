import "../../styles/mockexam.css";
import { useTranslation } from "react-i18next";

const ExamInstructions = () => {
    const { t } = useTranslation();

    return (
        <section className="mockExam-section">

            <h2>{t("exam.instructions.title")}</h2>

            <ul className="instructions-list">
                <li>{t("exam.instructions.questions")}</li>
                <li>{t("exam.instructions.time")}</li>
                <li>{t("exam.instructions.oneCorrect")}</li>
                <li>{t("exam.instructions.review")}</li>
                <li>{t("exam.instructions.score")}</li>
                <li>{t("exam.instructions.unanswered")}</li>
                <li>{t("exam.instructions.noMinusPoints")}</li>
            </ul>

        </section>
    );
};

export default ExamInstructions;
