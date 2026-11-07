import "../../styles/mockexam.css";
import {useTranslation} from "react-i18next";

const MockExamHeader = () => {
    const { t } = useTranslation();

    return (
        <section className="mockExam-header" >
            <h2>{t("mockExam.headerTitle")}</h2>

            <div className="exam-details">
                <div className="exam-detail">
                    <span className="label">{t("mockExam.subjectLabel")}</span>
                    <strong>{t("mockExam.subjectValue")}</strong>
                </div>
                <div className="exam-detail">
                    <span className="label">{t("mockExam.questionsLabel")}</span>
                    <strong>{t("mockExam.questionsValue")}</strong>
                </div>
                <div className="exam-detail">
                    <span className="label">{t("mockExam.timeLabel")}</span>
                    <strong>{t("mockExam.timeValue")}</strong>
                </div>
            </div>

        </section>

    );
};

export default MockExamHeader;
