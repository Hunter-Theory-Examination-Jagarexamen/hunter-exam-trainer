import {useTranslation} from "react-i18next";
import PageTitle from "../../components/common/PageTitle.tsx";
import StatisticsCards from "../../components/dashboard/StatisticsCards.tsx";
import "../../styles/dashboard.css"
import QuickActions from "../../components/dashboard/QuickActions.tsx";
import RecentActivityList from "../../components/dashboard/RecentActivityList.tsx";

const Dashboard = () => {
    const {t} = useTranslation();

    return (
        <div className="dashboard">
            <PageTitle
                title={t("dashboard.title")}
                subtitle={t("dashboard.subtitle")}
            />
            <StatisticsCards />

            <QuickActions />

            <RecentActivityList />
        </div>
    );
};

export default Dashboard;
