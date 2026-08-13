import "../../styles/statistics.css";
import PageTitle from "../../components/common/PageTitle";
import SummaryCards from "../../components/statistics/SummaryCards";
import ProgressBar from "../../components/statistics/ProgressBar";
import RecentResults from "../../components/statistics/RecentResults";
import PerformanceSummary from "../../components/statistics/PerformanceSummary";

const Statistics = () => {
    return (
        <div className="statistics">
            <PageTitle
                title="Statistics"
                subtitle="View your learning progress."
            />
            <SummaryCards />

            <section className="dashboard-section">
                <h2>Performance by Subject</h2>
                <ProgressBar subject="Hunting Laws" percentage={65}/>
                <ProgressBar subject="Weapons & Equipment" percentage={74}/>
                <ProgressBar subject="Dog Handling" percentage={58}/>
            </section>

            <section className="dashboard-section">
                <h2>Recent Results</h2>
                <div className="statistics-card">
                    <RecentResults practice="Practice Session #12" percentage={82} />
                    <RecentResults practice="Practice Session #11" percentage={74} />
                    <RecentResults practice="Mock Exam #2" percentage={68} />
                </div>
            </section>

            <section className="dashboard-section">
                <h2>Performance Summary</h2>
                <div className="statistics-card">
                    <PerformanceSummary
                        strongestSubject="Safety"
                        needsImprovement="Dog Handling"
                    />
                </div>
            </section>
        </div>
    );
};

export default Statistics;