package com.hunterexam.backend.repository.projection;

import java.time.LocalDateTime;

/**
 * When a question was answered and whether the answer was right: just the two
 * columns the "progress over time" chart needs, not the whole
 * {@code AttemptQuestion} row (see {@code MockExamScore} for why query
 * results like this get their own type).
 *
 * @param answeredAt when the student answered
 * @param correct    whether the answer was right
 */
public record AnswerOutcome(LocalDateTime answeredAt, Boolean correct) {

    // Boolean (not boolean) to match the entity's column type exactly, which
    // JPQL's "SELECT new" needs. Answered rows always have a value.
    public boolean isCorrect() {
        return Boolean.TRUE.equals(correct);
    }
}
