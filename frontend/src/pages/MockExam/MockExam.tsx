import PageTitle from "../../components/common/PageTitle";
import MockExamHeader from "../../components/mockexam/MockExamHeader";
import ExamInstructions from "../../components/mockexam/ExamInstructions";
import StartExam from "../../components/mockexam/StartExam";
import "../../styles/mockexam.css";
import {useNavigate} from "react-router-dom";

const MockExam = () => {

    const navigate = useNavigate();

    const handleStartExam = () => {
        navigate("/exam");
    };

    return (
        <div className="mockExam">

            <PageTitle
                title="Övningsprov"
                subtitle="Förbered dig med en fullständig provsimulering."
            />
            <MockExamHeader />
            <ExamInstructions />
            <StartExam onStartExam={handleStartExam} />

        </div>

    );

};

export default MockExam;
