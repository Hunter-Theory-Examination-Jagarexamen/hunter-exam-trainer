package com.hunterexam.backend.dto;

import java.time.LocalDate;

/**
 * Represents a user's practice accuracy for a single week.
 *
 * @param weekStart          Monday of the week (ISO)
 * @param accuracy           accuracy as a whole number 0–100
 * @param questionsAnswered  total questions answered that week
 */
public record ProgressOverTimeResponse(
        LocalDate weekStart,
        long accuracy,
        long questionsAnswered
) {}