package com.hunterexam.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request data used when practicing the exam.
 */
@Getter
@Setter
@NoArgsConstructor
public class PracticeResultRequest {

    private Long subjectId;
    private long correctAnswers;
    private long totalQuestions;
}
