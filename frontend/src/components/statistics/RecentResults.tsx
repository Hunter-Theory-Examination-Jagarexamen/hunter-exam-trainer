import {Check} from "lucide-react";
import {useTranslation} from "react-i18next";
import "../../styles/statistics.css"
import type { RecentActivity } from "../../types/dashboardInfo.ts";

interface RecentResultsProps {
    activity: RecentActivity;
}

const RecentResults = (
    { activity }: RecentResultsProps) => {
    const { t } = useTranslation();

    return (
        <div className="recent-results">
            <Check size={16} className="recent-result-icon" />

            <span className="recent-result-name">
                {t("statistics.practiceActivityLabel")}
            </span>
            <span className="recent-result-score">
                {activity.score}%
            </span>
            <span className="recent-result-date">
                {new Date(activity.completedAt).toLocaleDateString(
                    "sv-SE",
                    {
                        month: "short",
                        day: "numeric",
                        year: "numeric"
                    }
                )}
            </span>
        </div>
    );
};

export default RecentResults;
