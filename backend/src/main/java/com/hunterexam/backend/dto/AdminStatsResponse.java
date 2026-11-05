package com.hunterexam.backend.dto;

/**
 * Counts shown as cards on the admin start page.
 *
 * @param questionCount total questions across all subjects
 * @param subjectCount  total subjects
 * @param studentCount  users with the STUDENT role, including inactive accounts
 */
public record AdminStatsResponse(
        long questionCount,
        long subjectCount,
        long studentCount
) {}
