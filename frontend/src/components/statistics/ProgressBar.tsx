import "../../styles/statistics.css"

interface ProgressBarProps {
    subject: string;
    percentage: number;
}

const ProgressBar = (
    { subject, percentage }: ProgressBarProps) => {

    return (
        <section className="subject-progress">
            <div className="subject-header">
                <span>{subject}</span>
                <span>{percentage}%</span>
            </div>
            <div className="progress-bar">
                <div className="progress-fill" style={{ width: `${percentage}%` }}>

                </div>
            </div>
        </section>
    );
};

export default ProgressBar;