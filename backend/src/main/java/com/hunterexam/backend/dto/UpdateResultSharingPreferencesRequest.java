package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Replaces the authenticated student's sharing choices. Both fields are
 * required; the owner is taken from the security context, never from the body.
 */
public record UpdateResultSharingPreferencesRequest(
        @NotNull(message = "shareSubjectStatistics is required")
        Boolean shareSubjectStatistics,

        @NotNull(message = "shareMockExamResults is required")
        Boolean shareMockExamResults
) {}
