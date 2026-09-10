package com.hunterexam.backend.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RecentActivityResponse {

    private Long id;
    private long correctAnswers;
    private long totalQuestions;
    private long score;
    private LocalDateTime completedAt;

    public RecentActivityResponse(
            Long id,
            long correctAnswers,
            long totalQuestions,
            long score,
            LocalDateTime completedAt
    ) {
        this.id = id;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.score = score;
        this.completedAt = completedAt;
    }
}
