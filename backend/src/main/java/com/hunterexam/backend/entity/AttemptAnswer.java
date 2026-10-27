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
 * One answer given to one question in an {@link Attempt}.
 * <p>
 * This is the raw data that statistics, history and review are built from.
 * The subject comes from the question, so mixed rounds need nothing special.
 */
@Entity
@Table(
        name = "attempt_answers",
        // The database itself refuses a second answer to the same question in one round.
        uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "question_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // The chosen option as a letter: "A", "B", "C" or "D".
    @Column(nullable = false, length = 1)
    private String chosenOption;

    // Saved when answering, so later edits to the question don't change old results.
    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private LocalDateTime answeredAt;

    public AttemptAnswer(Attempt attempt, Question question, String chosenOption,
                         boolean correct, LocalDateTime answeredAt) {
        this.attempt = attempt;
        this.question = question;
        this.chosenOption = chosenOption;
        this.correct = correct;
        this.answeredAt = answeredAt;
    }
}
