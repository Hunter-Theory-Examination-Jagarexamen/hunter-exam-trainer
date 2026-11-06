import {useTranslation} from "react-i18next";
import "../../styles/dashboard.css";
import {useNavigate} from "react-router-dom";

const QuickActions = () => {
    const {t} = useTranslation();
    const navigate = useNavigate();

    return (
        <section className="dashboard-section">
            <h2>{t("dashboard.quickActionsTitle")}</h2>

            <div className="quick-actions">
                <button
                    onClick={() => navigate("/practice")}
                >
                    {t("dashboard.startPractice")}
                </button>
                <button
                    onClick={() => navigate("/mockexam")}
                >
                    {t("dashboard.startMockExam")}
                </button>
            </div>
        </section>
    );
};

export default QuickActions;
