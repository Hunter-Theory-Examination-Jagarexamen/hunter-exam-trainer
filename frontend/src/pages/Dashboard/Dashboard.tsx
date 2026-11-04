import PageTitle from "../../components/common/PageTitle.tsx";
import StatisticsCards from "../../components/dashboard/StatisticsCards.tsx";
import "../../styles/dashboard.css"
import QuickActions from "../../components/dashboard/QuickActions.tsx";
import RecentActivityList from "../../components/dashboard/RecentActivityList.tsx";

const Dashboard = () => {
    return (
        <div className="dashboard">
            <PageTitle
                title="Instrumentpanel"
                subtitle="Följ dina inlärningsframsteg."
            />
            <StatisticsCards />

            <QuickActions />

            <RecentActivityList />
        </div>
    );
};

export default Dashboard;
