package com.hunterexam.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents the result of a single completed practice session.
 * <p>
 * Stores how many questions were answered correctly for a given subject,
 * linked to the user who completed the session. Used to calculate
 * subject performance statistics.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PracticeResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long correctAnswers;

    private long totalQuestions;

    // Percentage score (0–100), calculated from correctAnswers / totalQuestions.
    private long score;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
