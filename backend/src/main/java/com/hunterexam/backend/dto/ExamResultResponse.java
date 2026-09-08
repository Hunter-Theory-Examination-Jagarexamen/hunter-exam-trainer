package com.hunterexam.backend.dto;

import lombok.Getter;

@Getter
public class ExamResultResponse {

    private long totalQuestions;
    private long correctAnswers;
    private long incorrectAnswers;
    private long unanswered;
    private long score;

    public ExamResultResponse(
            long totalQuestions,
            long correctAnswers,
            long incorrectAnswers,
            long unanswered,
            long score
    ) {
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.unanswered = unanswered;
        this.score = score;
    }
}
