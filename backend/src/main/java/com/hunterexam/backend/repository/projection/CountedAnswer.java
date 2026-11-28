package com.hunterexam.backend.repository.projection;

import java.time.LocalDateTime;

/**
 * One answer that counts toward the student's statistics: given in practice,
 * in a scored mock exam, or within the time limit of a mock exam that ended
 * without a result. "Counted" means counted in, not a number.
 * <p>
 * {@code AttemptQuestionRepository.findCountedAnswers} returns every such
 * answer, oldest first: the whole history, so the chart can replay it day by
 * day. {@code StatisticsService} then keeps each question's latest answer to
 * decide which questions are mastered.
 * <p>
 * Why a separate type: the query returns a join of answer, attempt and
 * question, not a table row, so no entity fits. Without a type, JPQL would
 * return an untyped {@code Object[]} per row (see {@code MockExamScore} for
 * the same reason).
 *
 * @param questionId the question that was answered
 * @param subjectId  the question's current subject (the denominator side)
 * @param answeredAt when the answer was given; the order answers are given in
 * @param correct    whether the answer was right (Boolean to match the column)
 */
public record CountedAnswer(
        Long questionId,
        Long subjectId,
        LocalDateTime answeredAt,
        Boolean correct
) {

    /** Whether this answer was right; answered rows always have a value. */
    public boolean isCorrect() {
        return Boolean.TRUE.equals(correct);
    }
}
