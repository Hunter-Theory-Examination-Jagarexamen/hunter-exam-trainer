package com.hunterexam.backend.repository;

import com.hunterexam.backend.repository.projection.MockExamScore;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
