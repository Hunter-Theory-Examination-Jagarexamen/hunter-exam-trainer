import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
} from "recharts";
import type { ProgressPoint } from "../../types/progressOverTime.ts";

interface Props {
    data: ProgressPoint[];
}

const ProgressOverTimeChart = ({ data }: Props) => {
    if (data.length === 0) {
        return <p>No practice results yet.</p>;
    }

    const chartData = data.map((point) => ({
        week: new Date(point.weekStart).toLocaleDateString("sv-SE", {
            month: "short",
            day: "numeric",
        }),
        accuracy: point.accuracy,
        questionsAnswered: point.questionsAnswered,
    }));

    return (
        <ResponsiveContainer width="100%" height={280}>
            <LineChart data={chartData} margin={{ top: 10, right: 20, bottom: 0, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="week" />
                <YAxis domain={[0, 100]} unit="%" />
                <Tooltip
                    formatter={(value, name) => {
                        if (name === "accuracy") return [`${value}%`, "Accuracy"];
                        return [String(value), String(name)];
                    }}
                />
                <Line
                    type="monotone"
                    dataKey="accuracy"
                    stroke="#4a7c3f"
                    strokeWidth={2}
                    dot={{ r: 4 }}
                    activeDot={{ r: 6 }}
                />
            </LineChart>
        </ResponsiveContainer>
    );
};

export default ProgressOverTimeChart;