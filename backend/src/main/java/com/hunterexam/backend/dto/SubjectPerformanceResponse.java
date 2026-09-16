package com.hunterexam.backend.dto;

import lombok.Getter;

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
