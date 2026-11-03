package com.hunterexam.backend.repository.projection;

import java.time.LocalDateTime;

/**
 * One scored mock exam, counted by the database from the exam's rows
 * (see {@code AttemptQuestionRepository.findScoredMockExams}).
 * Not sent to the frontend; the services build their responses from it.
 * <p>
 * Why a separate type: the query returns counted values (one row per exam),
 * not a table row, so no entity fits ({@code Attempt} stores no score). Without
 * a type, JPQL would return an untyped {@code Object[]} per row. A query result
 * like this is called a "projection" (Spring Data's term); it lives next to
 * the repositories because it belongs to the database side, while
 * {@code dto/} holds what the API sends and receives.
 *
 * @param attemptId  the exam's attempt
 * @param userId     the student
 * @param finishedAt when it was submitted
 * @param total      questions in the exam
 * @param correct    correct answers (unanswered count as not correct)
 */
public record MockExamScore(
        Long attemptId,
        Long userId,
        LocalDateTime finishedAt,
        Long total,
        Long correct
) {
    /** Rounded percentage, like the result page after submit. */
    public long score() {
        return total == 0 ? 0 : Math.round(correct * 100.0 / total);
    }
}
