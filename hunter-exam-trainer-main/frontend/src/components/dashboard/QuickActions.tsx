import "../../styles/dashboard.css"
import {useNavigate} from "react-router-dom";

const QuickActions = () => {

    const navigate = useNavigate();

    return (
        <section className="dashboard-section">
            <h2>Quick Actions</h2>

            <div className="quick-actions">
                <button
                    onClick={() => navigate("/practice")}
                >
                    Start Practice
                </button>
                <button
                    onClick={() => navigate("/mockexam")}
                >
                    Start Mock Exam
                </button>
            </div>
        </section>
    );
};

export default QuickActions;