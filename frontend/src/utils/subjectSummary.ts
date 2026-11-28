import type { SubjectPerformance } from "../types/subjectPerformance.ts";

export interface SubjectSummary {
    strongest: SubjectPerformance;
    /** Missing when only one subject is started: one subject in both places would look odd. */
    weakest?: SubjectPerformance;
}

/**
 * The strongest and the weakest subject by question mastery, for the
 * Statistics page's summary.
 *
 * Only subjects that have questions and that the student has started count:
 * an empty subject has nothing to master, and an unstarted one says nothing
 * about the student yet (#193). Ties keep the first subject (the bank's order).
 *
 * @returns null when no subject is started, so the page can say "no results yet"
 */
export const summarizeSubjects = (subjects: SubjectPerformance[]): SubjectSummary | null => {
    const started = subjects.filter((subject) => subject.total > 0 && subject.answered > 0);
    if (started.length === 0) {
        return null;
    }

    const strongest = started.reduce((best, subject) =>
        subject.percentage > best.percentage ? subject : best
    );

    // The weakest is picked among the others, so it is never the strongest too.
    const others = started.filter((subject) => subject !== strongest);
    const weakest = others.length > 0
        ? others.reduce((worst, subject) =>
            subject.percentage < worst.percentage ? subject : worst
        )
        : undefined;

    return { strongest, weakest };
};
