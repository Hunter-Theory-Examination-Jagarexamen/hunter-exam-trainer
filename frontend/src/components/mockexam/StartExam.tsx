import "../../styles/mockexam.css";
import {useTranslation} from "react-i18next";

interface StartExamProps {
    onStartExam: () => void;
}

const StartExam = ({ onStartExam }: StartExamProps) => {
    const { t } = useTranslation();

    return (
        <section className="mockexam-section">

            <button
                className="btn btn-primary"
                onClick={onStartExam}
            >
                {t("mockExam.startButton")}
            </button>

        </section>
    );
};

export default StartExam;
