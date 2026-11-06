import {BookOpenText, Trophy} from "lucide-react";
import {useTranslation} from "react-i18next";
import "../../styles/statistics.css";

interface PerformanceSummaryProps {
    strongestSubject: string;
    needsImprovement: string;
}

const PerformanceSummary = (
    { strongestSubject, needsImprovement }: PerformanceSummaryProps) => {
    const { t } = useTranslation();

    return (
        <section>
            <div className="performance-result">
                <Trophy size={16} className="performance-icon" />
                <span className="subject-title">
                     {t("statistics.strongestSubject")}
                </span>
                <span className="subject-name">
                    {strongestSubject}
                </span>
            </div>
            <div className="performance-result">
                <BookOpenText size={16} className="performance-icon" />
                <span className="subject-title">
                    {t("statistics.needsImprovement")}
                </span>
                <span className="subject-name">
                    {needsImprovement}
                </span>
            </div>
        </section>
    );
};

export default PerformanceSummary;
