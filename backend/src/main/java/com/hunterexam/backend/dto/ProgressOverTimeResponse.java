package com.hunterexam.backend.dto;

import java.time.LocalDate;

/**
 * One day's question-bank mastery: the share of <em>all</em> questions in the
 * bank the student had mastered at the end of that day.
 * <p>
 * A day is a calendar day in the server's default time zone (the zone the
 * stored {@code answered_at} values are read in), and "end of day" means the
 * last instant of that day. Days without answers repeat the previous day's
 * state, so the series only moves when the student's latest answer to a
 * question changes.
 *
 * @param date              the day the value describes (inclusive, oldest first)
 * @param masteryPercentage {@link #masteredQuestions} ÷ {@link #totalQuestions}, rounded
 * @param masteredQuestions questions mastered at the end of that day
 * @param totalQuestions    questions in the bank now (see the API
 *                          specification: historical points use the current
 *                          bank size, because the schema stores no historical
 *                          bank size)
 */
public record ProgressOverTimeResponse(
        LocalDate date,
        long masteryPercentage,
        long masteredQuestions,
        long totalQuestions
) {}
