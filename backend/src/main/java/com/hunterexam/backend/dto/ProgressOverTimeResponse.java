package com.hunterexam.backend.dto;

import java.time.LocalDate;

/**
 * How much of the question bank the student had mastered at one point in time.
 *
 * @param date       the day, or the Monday of the week when the points are per week
 * @param percentage mastered / total as a whole number 0–100
 * @param mastered   questions whose latest answer was correct
 * @param total      questions in the bank
 */
public record ProgressOverTimeResponse(
        LocalDate date,
        long percentage,
        long mastered,
        long total
) {}
