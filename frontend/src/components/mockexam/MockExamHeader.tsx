import "../../styles/mockexam.css";

const MockExamHeader = () => {
    return (
        <section className="mockexam-header" >
            <h2 >Exam Overview</h2>

            <div className="exam-details">
                <div className="exam-detail">
                    <span className="label">Subject</span>
                    <strong>All Categories</strong>
                </div>
                <div className="exam-detail">
                    <span className="label">Questions</span>
                    <strong>70</strong>
                </div>
                <div className="exam-detail">
                    <span className="label">Time</span>
                    <strong>60 Minutes</strong>
                </div>
            </div>

        </section>

    );
};

export default MockExamHeader;