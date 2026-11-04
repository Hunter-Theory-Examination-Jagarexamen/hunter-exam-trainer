package com.hunterexam.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One round of questions taken by a user (a practice round or a mock exam).
 * <p>
 * The round itself has no subject: each answer's question has one, so a
 * round can mix subjects. Totals are not stored here; they are counted
 * from the round's answers.
 */
@Entity
@Table(name = "attempts")
@Getter
@Setter
@NoArgsConstructor
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Stored as text ("SUBJECT_PRACTICE"), so reordering the enum can't change old rows.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptType type;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    // Empty (null) while the round is running.
    private LocalDateTime finishedAt;

    // True only for a mock exam that was submitted in time and scored. A late
    // submit closes the exam (finishedAt is set) but leaves this false.
    // The default makes ddl-auto=update able to add the column to existing rows.
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean scored;

    public Attempt(User user, AttemptType type, LocalDateTime startedAt) {
        this.user = user;
        this.type = type;
        this.startedAt = startedAt;
    }

    public boolean isFinished() {
        return finishedAt != null;
    }
}
