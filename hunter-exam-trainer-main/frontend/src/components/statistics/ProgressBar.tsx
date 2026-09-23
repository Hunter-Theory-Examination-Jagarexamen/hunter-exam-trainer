import "../../styles/statistics.css"

interface ProgressBarProps {
    subject: string;
    percentage: number;
    onClick?: () => void;
}

const ProgressBar = (
    { subject, percentage, onClick }: ProgressBarProps) => {

    return (
        <button
            type="button"
            className="subject-progress"
            onClick={onClick}
        >
            <div className="subject-header">
                <span>{subject}</span>
                <span>{percentage}%</span>
            </div>
            <div className="progress-bar">
                <div className="progress-fill" style={{ width: `${percentage}%` }}>

                </div>
            </div>
        </button>
    );
};

export default ProgressBar;