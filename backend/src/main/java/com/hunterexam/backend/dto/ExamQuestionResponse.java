package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * Response containing a question for the mock exam.
 * <p>
 * The correct answer is intentionally not included so that
 * it is not exposed to the client before the exam is submitted.
 */
@Getter
public class ExamQuestionResponse {

    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String imageUrl;

    public ExamQuestionResponse(
            Long id,
            String questionText,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
    String imageUrl)
    {
        this.id = id;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.imageUrl = imageUrl;
    }
}
