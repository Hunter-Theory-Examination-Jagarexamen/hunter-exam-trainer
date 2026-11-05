package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * A question as sent to the student, in a mock exam or a practice round.
 * <p>
 * The correct answer is intentionally not included, so the client
 * can't see it before the server has checked the answer.
 */
@Getter
public class QuestionResponse {

    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String imageUrl;

    public QuestionResponse(
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
