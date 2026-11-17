package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * Response returned after a successful mock exam submission.
 * <p>
 * {@code passed} comes from the server's scoring rule (#205) so the frontend
 * doesn't have to know the pass mark; the pass/fail text on the result page
 * is driven by this flag alone.
 */
@Getter
public class ExamSubmitResponse {

    private long totalQuestions;
    private long correctAnswers;
    private long incorrectAnswers;
    private long unanswered;
    private long score;
    private boolean passed;

    public ExamSubmitResponse(
        long totalQuestions,
        long correctAnswers,
        long incorrectAnswers,
        long unanswered,
        long score,
        boolean passed
    ) {
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.unanswered = unanswered;
        this.score = score;
        this.passed = passed;
    }
}
