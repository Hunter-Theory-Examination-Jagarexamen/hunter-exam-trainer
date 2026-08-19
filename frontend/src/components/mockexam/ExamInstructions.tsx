import "../../styles/mockexam.css";

const ExamInstructions = () => {
    return (
        <section className="mockexam-section">

            <h2>Instructions</h2>

            <ul className="instructions-list">
                <li>The exam contains 70 multiple-choice questions.</li>
                <li>You have 60 minutes to complete the exam.</li>
                <li>Each question has only one correct answer.</li>
                <li>You can review your answers before submitting.</li>
                <li>Your score will be displayed after submission.</li>
            </ul>

        </section>
    );
};

export default ExamInstructions;