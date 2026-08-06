import "../../styles/dashboard.css"

const QuickActions = () => {
    return (
        <section className="dashboard-section">
            <h2>Quick Actions</h2>

            <div className="quick-actions">
                <button>Start Practice</button>
                <button>Start Mock Exam</button>
            </div>
        </section>
    );
};

export default QuickActions;