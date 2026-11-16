package com.hunterexam.backend.repository;

import com.hunterexam.backend.repository.projection.CountedAnswer;
import com.hunterexam.backend.repository.projection.MockExamScore;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AttemptQuestionRepository extends JpaRepository<AttemptQuestion, Long> {

    // Spring builds the query from the method name: WHERE attempt.id = ? AND question.id = ?
    Optional<AttemptQuestion> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    // All questions of a round, in the order they were given.
    List<AttemptQuestion> findByAttemptIdOrderByPosition(Long attemptId);

    /**
     * Every scored mock exam of the given students, newest first, with its
     * score counted in the database: one row per exam ({@code GROUP BY}),
     * so the page needs one query for all students. Open and late (unscored)
     * exams are left out by {@code a.scored = true}.
     */
    @Query("""
            SELECT new com.hunterexam.backend.repository.projection.MockExamScore(
                a.id, a.user.id, a.finishedAt,
                COUNT(q),
                SUM(CASE WHEN q.correct = true THEN 1L ELSE 0L END))
            FROM AttemptQuestion q JOIN q.attempt a
            WHERE a.user IN :users
              AND a.type = com.hunterexam.backend.entity.AttemptType.MOCK_EXAM
              AND a.scored = true
            GROUP BY a.id, a.user.id, a.finishedAt
            ORDER BY a.finishedAt DESC
            """)
    List<MockExamScore> findScoredMockExams(@Param("users") Collection<User> users);

    /**
     * All of the student's counted answers, oldest first, with the question
     * and subject each answer belongs to. Same rule as {@code SubjectStats}:
     * every practice answer, plus mock exam answers when the exam was scored,
     * or when it ended without a result and the answer falls within the
     * exam's answer window (60 minutes + 2 minutes grace from the start, the
     * same bound {@code ExamService} uses). Rows not answered yet are left out.
     */
    @Query("""
            SELECT new com.hunterexam.backend.repository.projection.CountedAnswer(
                q.question.id, q.question.subject.id, q.answeredAt, q.correct)
            FROM AttemptQuestion q JOIN q.attempt a
            WHERE a.user = :user
              AND q.answeredAt IS NOT NULL
              AND (a.type <> com.hunterexam.backend.entity.AttemptType.MOCK_EXAM
                   OR a.scored = true
                   OR (a.finishedAt IS NOT NULL
                       AND q.answeredAt <= (a.startedAt + 62 minute)))
            ORDER BY q.answeredAt, q.id
            """)
    List<CountedAnswer> findCountedAnswers(@Param("user") User user);

    @Modifying
    @Query("delete from AttemptQuestion aq where aq.attempt.id in :attemptIds")
    int deleteByAttemptIds(@Param("attemptIds") Collection<Long> attemptIds);
}
