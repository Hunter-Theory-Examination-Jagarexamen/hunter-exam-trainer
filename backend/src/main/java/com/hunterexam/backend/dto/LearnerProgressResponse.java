package com.hunterexam.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregated progress information about a single learner,
 * for the admin "learner progress" view.
 *
 * @param userId            learner's id
 * @param fullName          learner's name
 * @param email             learner's email
 * @param createdAt         when the account was created
 * @param lastPracticedAt   timestamp of the most recent practice result, or null
 * @param subjectPerformances  per-subject accuracy (same shape as the user's own stats)
 */
public record LearnerProgressResponse(
        Long userId,
        String fullName,
        String email,
        LocalDateTime createdAt,
        LocalDateTime lastPracticedAt,
        List<SubjectPerformanceResponse> subjectPerformances
) {}