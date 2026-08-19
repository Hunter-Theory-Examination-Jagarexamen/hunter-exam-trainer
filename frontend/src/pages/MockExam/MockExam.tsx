import PageTitle from "../../components/common/PageTitle";

import MockExamHeader from "../../components/mockexam/MockExamHeader";

import ExamInstructions from "../../components/mockexam/ExamInstructions";

import StartExam from "../../components/mockexam/StartExam";

import "../../styles/mockexam.css";

const MockExam = () => {

    return (
        <div className="mockexam">
            <PageTitle

                title="Mock Exam"

                subtitle="Prepare yourself with a full exam simulation."

            />

            <MockExamHeader />

            <ExamInstructions />

            <StartExam />
        </div>

    );

};

export default MockExam;
