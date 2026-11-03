package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * Totals for a finished practice round, counted by the server from the
 * round's saved answers: {@code POST /api/practice/attempts/{id}/finish}.
 */
@Getter
public class PracticeFinishResponse {

    private final long correctAnswers;
    // Questions answered before the round was finished (skipped ones are not counted).
    private final long answeredQuestions;
    private final long totalQuestions;

    public PracticeFinishResponse(long correctAnswers, long answeredQuestions, long totalQuestions) {
        this.correctAnswers = correctAnswers;
        this.answeredQuestions = answeredQuestions;
        this.totalQuestions = totalQuestions;
    }
}
