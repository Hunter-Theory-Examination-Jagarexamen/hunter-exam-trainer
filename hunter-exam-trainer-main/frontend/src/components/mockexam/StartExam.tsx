import "../../styles/mockexam.css";

interface StartExamProps {
    onStartExam: () => void;
}

const StartExam = ({ onStartExam }: StartExamProps) => {
    return (
        <section className="mockexam-section">

            <button
                className="start-exam-btn"
                onClick={onStartExam}
            >
                Start Mock Exam
            </button>

        </section>
    );
};

export default StartExam;