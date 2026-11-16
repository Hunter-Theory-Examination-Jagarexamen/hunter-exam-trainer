package com.hunterexam.backend.dto;

/**
 * The authenticated student's sharing choices for course leaders (#125).
 * <p>
 * {@code shareSubjectStatistics} covers everything the admin's Learner
 * Progress shows as per-subject performance (practice and mock exam answers
 * alike, because the per-subject totals mix both). {@code shareMockExamResults}
 * covers the scored mock exam summary. Both are {@code false} by default.
 */
public record ResultSharingPreferencesResponse(
        boolean shareSubjectStatistics,
        boolean shareMockExamResults
) {}
