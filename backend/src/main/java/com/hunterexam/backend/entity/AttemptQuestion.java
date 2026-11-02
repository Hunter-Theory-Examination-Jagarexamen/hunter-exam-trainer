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
 * One question in an {@link Attempt}, and the student's answer to it.
 * <p>
 * All rows of a round are created when the round starts, with the answer
 * fields empty. They are filled in when the student answers, so skipped
 * questions stay visible (useful for reviewing old quizzes). The subject comes
 * from the question, so mixed rounds need nothing special.
 */
@Entity
@Table(
        name = "attempt_questions",
        uniqueConstraints = {
                // The same question can't appear twice in one round.
                @UniqueConstraint(columnNames = {"attempt_id", "question_id"}),
                @UniqueConstraint(columnNames = {"attempt_id", "position"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AttemptQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // The question's place in the round: 0, 1, 2, ...
    @Column(nullable = false)
    private int position;

    // The chosen option as a letter ("A"–"D"); null until answered.
    @Column(length = 1)
    private String chosenOption;

    // Boolean (not boolean) so it can be null until answered.
    // Saved when answering, so later edits to the question don't change old results.
    private Boolean correct;

    private LocalDateTime answeredAt;

    public AttemptQuestion(Attempt attempt, Question question, int position) {
        this.attempt = attempt;
        this.question = question;
        this.position = position;
    }

    public boolean isAnswered() {
        return answeredAt != null;
    }

    /** Fills in the answer. The service checks first that it isn't answered already. */
    public void recordAnswer(String chosenOption, boolean correct, LocalDateTime answeredAt) {
        this.chosenOption = chosenOption;
        this.correct = correct;
        this.answeredAt = answeredAt;
    }
}
