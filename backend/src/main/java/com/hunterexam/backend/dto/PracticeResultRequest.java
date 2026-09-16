package com.hunterexam.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PracticeResultRequest {

    private Long subjectId;
    private long correctAnswers;
    private long totalQuestions;
}
