package com.hunterexam.backend.dto;

import lombok.Getter;

@Getter
public class DashboardResponse {

    private long mockExams;
    private long questionsAnswered;
    private long averageScore;
    private long bestScore;

    public DashboardResponse(
            long mockExams,
            long questionsAnswered,
            long averageScore,
            long bestScore
    ) {
        this.mockExams = mockExams;
        this.questionsAnswered = questionsAnswered;
        this.averageScore = averageScore;
        this.bestScore = bestScore;
    }
}
