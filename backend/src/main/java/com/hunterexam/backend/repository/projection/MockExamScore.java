package com.hunterexam.backend.repository.projection;

import java.time.LocalDateTime;

/**
 * One scored mock exam, counted by the database from the exam's rows
 * (see {@code AttemptQuestionRepository.findScoredMockExams}).
 * Not sent to the frontend; the services build their responses from it.
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
