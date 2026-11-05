package com.hunterexam.backend.dto;

import java.time.LocalDateTime;

public record MockExamSummary(
        long count,
        Integer latestScore,
        LocalDateTime latestAt,
        Integer bestScore,
        Boolean latestPassed
) {
    public static MockExamSummary empty() {
        return new MockExamSummary(0, null, null, null, null);
    }
}
