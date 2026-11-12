package com.hunterexam.backend.repository.projection;

import java.time.LocalDateTime;

/**
 * One counted answer: which question (and its subject), when, and whether it
 * was right. What the mastery statistics need to know, per answer, to find
 * each question's latest result (see {@code MockExamScore} for why query
 * results get their own type).
 *
 * @param questionId the question answered
 * @param subjectId  the question's subject
 * @param answeredAt when the student answered
 * @param correct    whether the answer was right
 */
public record CountedAnswer(Long questionId, Long subjectId, LocalDateTime answeredAt, Boolean correct) {

    // Boolean (not boolean) to match the entity's column type, which JPQL's
    // "SELECT new" needs. Answered rows always have a value.
    public boolean isCorrect() {
        return Boolean.TRUE.equals(correct);
    }
}
