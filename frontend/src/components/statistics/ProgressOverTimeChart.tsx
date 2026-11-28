import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
} from "recharts";
import { useEffect, useRef, useState } from "react";
import {useTranslation} from "react-i18next";
import type { ProgressPoint } from "../../types/progressOverTime.ts";
import { dateLocale, fullDate, xAxisLabels } from "../../utils/chartDates.ts";

interface Props {
    data: ProgressPoint[];
}

/** What Recharts passes to a dot renderer: the point's position and data. */
interface DotProps {
    cx?: number;
    cy?: number;
    index?: number;
    payload?: ProgressPoint;
}

/** A small bead on days (or weeks) with practice; nothing on the others. */
const renderBead = ({ cx, cy, index, payload }: DotProps) =>
    payload?.practised && cx !== undefined && cy !== undefined
        ? <circle key={`bead-${index}`} cx={cx} cy={cy} r={2.5} fill="currentColor" />
        : <g key={`bead-${index}`} />;

const ProgressOverTimeChart = ({ data }: Props) => {
    const { t, i18n } = useTranslation();

    // On a phone a tapped tooltip stays open; a tap outside the chart closes it.
    const chartRef = useRef<HTMLDivElement>(null);
    const [tooltipClosed, setTooltipClosed] = useState(false);

    useEffect(() => {
        const closeOnTapOutside = (event: PointerEvent) => {
            setTooltipClosed(!chartRef.current?.contains(event.target as Node));
        };
        document.addEventListener("pointerdown", closeOnTapOutside);
        return () => document.removeEventListener("pointerdown", closeOnTapOutside);
    }, []);

    if (data.length === 0) {
        return <p>{t("statistics.noProgressData")}</p>;
    }

    const locale = dateLocale(i18n.language);
    const labels = xAxisLabels(data.map((point) => point.date), locale);

    return (
        <div ref={chartRef}>
            <ResponsiveContainer width="100%" height={280}>
                <LineChart data={data} margin={{ top: 10, right: 20, bottom: 0, left: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                        dataKey="date"
                        ticks={[...labels.keys()]}
                        interval={0}
                        tickFormatter={(date: string) => labels.get(date) ?? ""}
                        tickMargin={8}
                        padding={{ left: 12, right: 12 }}
                    />
                    <YAxis domain={[0, 100]} unit="%" />
                    <Tooltip
                        active={tooltipClosed ? false : undefined}
                        labelFormatter={(date) => fullDate(String(date), locale)}
                        formatter={(value, _name, item) => [
                            `${value}%`,
                            t("statistics.masteredCount", {
                                mastered: item.payload.mastered,
                                total: item.payload.total,
                            }),
                        ]}
                    />
                    <Line
                        type="linear"
                        dataKey="percentage"
                        stroke="currentColor"
                        strokeWidth={2}
                        dot={renderBead}
                        activeDot={{ r: 5 }}
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
};

export default ProgressOverTimeChart;
