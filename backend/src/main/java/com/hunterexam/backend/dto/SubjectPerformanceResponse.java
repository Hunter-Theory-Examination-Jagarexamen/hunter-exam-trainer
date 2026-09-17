package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * Response containing a user's practice performance for a subject.
 * <p>
 * Includes the subject ID, subject name, and calculated
 * performance percentage.
 */
@Getter
public class SubjectPerformanceResponse {

    private Long subjectId;
    private String subjectName;
    private long percentage;

    public SubjectPerformanceResponse(
            Long subjectId,
            String subjectName,
            long percentage
    ) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.percentage = percentage;
    }
}
