package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * The server's verdict on one practice answer. Sent only after the student
 * has answered, so the correct option is never known in advance.
 */
@Getter
public class PracticeAnswerResponse {

    private final boolean correct;

    // The right letter (A–D), so the page can mark it.
    private final String correctOption;

    // May be null: most questions have no explanation yet.
    private final String explanation;

    public PracticeAnswerResponse(boolean correct, String correctOption, String explanation) {
        this.correct = correct;
        this.correctOption = correctOption;
        this.explanation = explanation;
    }
}
