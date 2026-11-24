import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
} from "recharts";
import {useTranslation} from "react-i18next";
import type { ProgressPoint } from "../../types/progressOverTime.ts";

interface Props {
    data: ProgressPoint[];
}

const ProgressOverTimeChart = ({ data }: Props) => {
    const { t } = useTranslation();

    if (data.length === 0) {
        return <p>{t("statistics.noProgressData")}</p>;
    }

    const chartData = data.map((point) => ({
        date: new Date(`${point.date}T00:00:00`).toLocaleDateString(undefined, {
            month: "short",
            day: "numeric",
        }),
        masteryPercentage: point.masteryPercentage,
        masteredQuestions: point.masteredQuestions,
        totalQuestions: point.totalQuestions,
    }));

    return (
        <ResponsiveContainer width="100%" height={280}>
            <LineChart data={chartData} margin={{ top: 10, right: 20, bottom: 0, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis domain={[0, 100]} unit="%" />
                <Tooltip
                    formatter={(value, name) => {
                        if (name === "masteryPercentage") return [`${value}%`, t("statistics.masteryLabel")];
                        if (name === "masteredQuestions") return [
                            t("statistics.masteredCount", { mastered: value, total: chartData[0]?.totalQuestions ?? 0 }),
                            t("statistics.masteryLabel"),
                        ];
                        return [String(value), String(name)];
                    }}
                />
                <Line
                    type="monotone"
                    dataKey="masteryPercentage"
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
