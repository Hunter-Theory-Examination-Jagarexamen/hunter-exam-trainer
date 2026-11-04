import "../../styles/mockexam.css";

const MockExamHeader = () => {
    return (
        <section className="mockExam-header" >
            <h2>Provöversikt</h2>

            <div className="exam-details">
                <div className="exam-detail">
                    <span className="label">Ämne</span>
                    <strong>Alla kategorier</strong>
                </div>
                <div className="exam-detail">
                    <span className="label">Frågor</span>
                    <strong>70</strong>
                </div>
                <div className="exam-detail">
                    <span className="label">Tid</span>
                    <strong>60 minuter</strong>
                </div>
            </div>

        </section>

    );
};

export default MockExamHeader;
