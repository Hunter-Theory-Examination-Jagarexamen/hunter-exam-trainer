package com.hunterexam.backend.dto;

import com.hunterexam.backend.entity.Role;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One user's progress for the admin's detail view.
 * <p>
 * Sharing (#125): {@code subjectPerformances} and {@code mockExams} are
 * {@code null} when the student has not shared that category — never an empty
 * or zero-filled stand-in — and {@code lastActivityAt} is derived only from
 * shared categories ({@code null} when nothing shared has activity).
 */
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
