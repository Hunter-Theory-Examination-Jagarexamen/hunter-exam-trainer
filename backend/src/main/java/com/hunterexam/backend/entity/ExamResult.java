package com.hunterexam.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents the result of a completed mock exam attempt.
 * <p>
 * Stores the score breakdown (correct, incorrect, unanswered) for a single
 * exam, linked to the user who completed it.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int totalQuestions;

    private int correctAnswers;

    private int incorrectAnswers;

    private int unanswered;

    // Percentage score (0–100), calculated from correctAnswers / totalQuestions.
    private int score;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
