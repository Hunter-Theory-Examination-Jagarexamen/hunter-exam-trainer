package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * One subject's question mastery for a student.
 * <p>
 * The percentage is the share of the subject's questions the student has
 * <em>mastered</em> — whose latest counted answer is correct — not the share
 * of historically correct answers. The denominator is the subject's questions
 * in the bank right now, so unanswered questions count against mastery and the
 * value can reach 100 %.
 *
 * @see com.hunterexam.backend.service.StatisticsService
 */
@Getter
public class SubjectPerformanceResponse {

    private Long subjectId;
    private String subjectName;

    /** Questions whose latest counted answer is correct. */
    private long mastered;

    /** Questions the subject holds right now; 0 means "no questions yet". */
    private long total;

    /**
     * Questions in the subject the student has answered at least once (a
     * question answered three times counts once; 0 = not started). Not part
     * of the percentage; it tells "not started" apart from "nothing mastered
     * yet"
     */
    private long answered;

    /** {@link #mastered} ÷ {@link #total}, rounded; 0 when there are no questions. */
    private long percentage;

    public SubjectPerformanceResponse(
            Long subjectId,
            String subjectName,
            long mastered,
            long total,
            long answered,
            long percentage
    ) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.mastered = mastered;
        this.total = total;
        this.answered = answered;
        this.percentage = percentage;
    }
}
