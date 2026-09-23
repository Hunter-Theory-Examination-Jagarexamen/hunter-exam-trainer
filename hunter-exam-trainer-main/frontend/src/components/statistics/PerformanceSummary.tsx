import {BookOpenText, Trophy} from "lucide-react";
import "../../styles/statistics.css";

interface PerformanceSummaryProps {
    strongestSubject: string;
    needsImprovement: string;
}

const PerformanceSummary = (
    { strongestSubject, needsImprovement }: PerformanceSummaryProps) => {

    return (
        <section>
            <div className="performance-result">
                <Trophy size={16} className="performance-icon" />
                <span className="subject-title">
                    Strongest Subject
                </span>
                <span className="subject-name">
                    {strongestSubject}
                </span>
            </div>
            <div className="performance-result">
                <BookOpenText size={16} className="performance-icon" />
                <span className="subject-title">
                    Needs Improvement
                </span>
                <span className="subject-name">
                    {needsImprovement}
                </span>
            </div>
        </section>
    );
};

export default PerformanceSummary;