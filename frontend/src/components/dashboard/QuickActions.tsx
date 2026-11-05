import "../../styles/dashboard.css"
import {useNavigate} from "react-router-dom";

const QuickActions = () => {

    const navigate = useNavigate();

    return (
        <section className="dashboard-section">
            <h2>Snabbåtgärder</h2>

            <div className="quick-actions">
                <button
                    onClick={() => navigate("/practice")}
                >
                    Börja öva
                </button>
                <button
                    onClick={() => navigate("/mockexam")}
                >
                    Starta övningsprov
                </button>
            </div>
        </section>
    );
};

export default QuickActions;
