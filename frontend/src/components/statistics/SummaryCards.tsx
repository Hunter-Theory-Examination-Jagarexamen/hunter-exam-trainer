import Card from "../common/Card.tsx";
import "../../styles/statistics.css";

const SummaryCards = () => {
    return (
        <div className="statistics-cards">
            <Card title="Practice">
                12
            </Card>
            <Card title="Mock Exams">
                4
            </Card>
            <Card title="Avg Score">
                72%
            </Card>
            <Card title="Best Score">
                86%
            </Card>
        </div>
    );
};

export default SummaryCards;