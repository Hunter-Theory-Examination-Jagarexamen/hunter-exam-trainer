package com.hunterexam.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Running totals per student and subject, kept so statistics don't have to
 * count every {@link AttemptAnswer} on each page load.
 * <p>
 * Updated in the same transaction as each saved answer. Stores counts, not
 * percentages: the percentage is calculated when read. The answers remain the
 * source of truth, so this table can always be rebuilt from them.
 */
@Entity
@Table(
        name = "subject_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class SubjectStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private long answered;

    @Column(nullable = false)
    private long correct;

    private LocalDateTime lastAnsweredAt;

    public SubjectStats(User user, Subject subject) {
        this.user = user;
        this.subject = subject;
    }
}
