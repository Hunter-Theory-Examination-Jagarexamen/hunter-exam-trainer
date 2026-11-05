package com.hunterexam.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LearnerProgressResponse(
        Long userId,
        String fullName,
        String email,
        LocalDateTime createdAt,
        LocalDateTime lastActivityAt,
        List<SubjectPerformanceResponse> subjectPerformances,
        MockExamSummary mockExams
) {}