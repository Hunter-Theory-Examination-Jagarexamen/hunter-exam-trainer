import PageTitle from "../../components/common/PageTitle";
import MockExamHeader from "../../components/mockexam/MockExamHeader";
import ExamInstructions from "../../components/mockexam/ExamInstructions";
import StartExam from "../../components/mockexam/StartExam";
import "../../styles/mockexam.css";
import {useNavigate} from "react-router-dom";
import {useTranslation} from "react-i18next";

const MockExam = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const handleStartExam = () => {
        navigate("/exam");
    };

    return (
        <div className="mockExam">

            <PageTitle
                title={t("mockExam.title")}
                subtitle={t("mockExam.subtitle")}
            />
            <MockExamHeader />
            <ExamInstructions />
            <StartExam onStartExam={handleStartExam} />

        </div>

    );

};

export default MockExam;
