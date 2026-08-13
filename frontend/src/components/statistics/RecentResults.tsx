import {Check} from "lucide-react";
import "../../styles/statistics.css"

interface RecentResultsProps {
    practice: string;
    percentage: number;
}

const RecentResults = (
    { practice, percentage }: RecentResultsProps) => {

    return (
        <div className="recent-results">
            <Check size={16} className="result-icon" />
            <span className="result-name">
                {practice}
            </span>
            <span className="result-score">
                {percentage}%
            </span>
        </div>
    );
};

export default RecentResults;