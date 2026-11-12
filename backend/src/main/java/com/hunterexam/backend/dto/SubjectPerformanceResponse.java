package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * A student's mastery of one subject: how many of the subject's questions
 * were last answered correctly.
 */
@Getter
public class SubjectPerformanceResponse {

    private Long subjectId;
    private String subjectName;
    private long percentage;   // mastered / total, 0–100
    private long mastered;     // questions whose latest answer was correct
    private long total;        // questions in the subject

    public SubjectPerformanceResponse(
            Long subjectId,
            String subjectName,
            long percentage,
            long mastered,
            long total
    ) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.percentage = percentage;
        this.mastered = mastered;
        this.total = total;
    }
}
