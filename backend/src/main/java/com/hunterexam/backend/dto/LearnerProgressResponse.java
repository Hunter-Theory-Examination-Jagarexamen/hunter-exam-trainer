package com.hunterexam.backend.dto;

import com.hunterexam.backend.entity.Role;

import java.time.LocalDateTime;
import java.util.List;

public record LearnerProgressResponse(
        Long userId,
        String fullName,
        String email,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime lastActivityAt,
        List<SubjectPerformanceResponse> subjectPerformances,
        MockExamSummary mockExams
) {}
