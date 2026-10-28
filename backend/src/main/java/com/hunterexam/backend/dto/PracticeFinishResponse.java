package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * Totals for a finished practice round, counted by the server from the
 * round's saved answers: {@code POST /api/practice/attempts/{id}/finish}.
 */
@Getter
public class PracticeFinishResponse {

    private final long correctAnswers;
    private final long totalQuestions;

    public PracticeFinishResponse(long correctAnswers, long totalQuestions) {
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
    }
}
