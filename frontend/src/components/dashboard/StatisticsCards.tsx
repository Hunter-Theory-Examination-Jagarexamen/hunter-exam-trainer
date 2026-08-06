import Card from "../common/Card.tsx";
import "../../styles/dashboard.css"

const StatisticsCards = () => {
    return (
        <div className="dashboard-cards">
            <Card title="Practice Session" >
                0
            </Card>
            <Card title="Questions Answered" >
                0
            </Card>
            <Card title="Average Score" >
                0
            </Card>
            <Card title="Mock Exams" >
                0
            </Card>
        </div>
    );
};

export default StatisticsCards;