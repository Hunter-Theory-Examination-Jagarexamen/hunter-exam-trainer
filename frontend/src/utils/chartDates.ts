/** Date locale for the app's language: Swedish unless the app is in English. */
export const dateLocale = (language: string): string =>
    language.startsWith("en") ? "en-GB" : "sv-SE";

/** An ISO date ("2026-11-02") as local midnight, so the day never shifts. */
const toDate = (isoDate: string): Date => new Date(`${isoDate}T00:00:00`);

/** Day number and short month without a trailing dot: "2 nov", "2 Nov". */
const dayAndMonth = (isoDate: string, locale: string): { day: string; month: string } => {
    const parts = new Intl.DateTimeFormat(locale, { day: "numeric", month: "short" })
        .formatToParts(toDate(isoDate));
    return {
        day: parts.find((part) => part.type === "day")?.value ?? "",
        month: (parts.find((part) => part.type === "month")?.value ?? "").replace(".", ""),
    };
};

/**
 * Which points get a label on the x-axis: about four in every view, so the
 * labels never collide on a phone. Mondays for daily points; for weekly
 * points every second (8 weeks) or third (12 weeks) week, counted back from
 * the last, so the current week is always labelled.
 */
const tickDates = (dates: string[]): string[] => {
    if (dates.length < 2) {
        return dates;
    }

    const daysBetweenPoints =
        (toDate(dates[1]).getTime() - toDate(dates[0]).getTime()) / 86_400_000;

    if (daysBetweenPoints === 1) {
        return dates.filter((date) => toDate(date).getDay() === 1);
    }
    const step = Math.ceil(dates.length / 4);
    return dates.filter((_, index) => (dates.length - 1 - index) % step === 0);
};

/**
 * The x-axis labels, keyed by the points' dates (oldest first): the day
 * number, with the month only on the first label and where a new month
 * starts ("26 okt  2 nov  9  16  23  30  7 dec").
 */
export const xAxisLabels = (dates: string[], locale: string): Map<string, string> => {
    const labels = new Map<string, string>();
    let previousMonth = "";

    for (const date of tickDates(dates)) {
        const { day, month } = dayAndMonth(date, locale);
        labels.set(date, month === previousMonth ? day : `${day} ${month}`);
        previousMonth = month;
    }
    return labels;
};

/** The full date for the tooltip: "2 november". */
export const fullDate = (isoDate: string, locale: string): string =>
    toDate(isoDate).toLocaleDateString(locale, { day: "numeric", month: "long" });
