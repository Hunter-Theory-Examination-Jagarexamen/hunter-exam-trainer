import PageTitle from "../../components/common/PageTitle.tsx";
import StatisticsCards from "../../components/dashboard/StatisticsCards.tsx";
import "../../styles/dashboard.css"
import QuickActions from "../../components/dashboard/QuickActions.tsx";
import RecentActivity from "../../components/dashboard/RecentActivity.tsx";

const Dashboard = () => {
    return (
        <div className="dashboard">
            <PageTitle
                title="Dashboard"
                subtitle="Track your learning progress."
            />
            <StatisticsCards />

            <QuickActions />

            <RecentActivity />
        </div>
    );
};

export default Dashboard;