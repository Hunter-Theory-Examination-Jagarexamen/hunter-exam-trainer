package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface SubjectStatsRepository extends JpaRepository<SubjectStats, Long> {

    boolean existsByUserAndSubject(User user, Subject subject);

    /**
     * The student's totals for every subject with at least one answer, with
     * the subject loaded in the same query ({@code JOIN FETCH}), ordered by
     * subject id. Rows with 0 answers exist when a round was started but
     * nothing was answered yet; they are left out.
     */
    @Query("""
            SELECT s FROM SubjectStats s
            JOIN FETCH s.subject
            WHERE s.user = :user AND s.answered > 0
            ORDER BY s.subject.id
            """)
    List<SubjectStats> findAnsweredByUser(@Param("user") User user);

    /**
     * Same as {@link #findAnsweredByUser}, for many students in one query
     * (Learner Progress), instead of one query per student.
     */
    @Query("""
            SELECT s FROM SubjectStats s
            JOIN FETCH s.subject
            WHERE s.user IN :users AND s.answered > 0
            ORDER BY s.subject.id
            """)
    List<SubjectStats> findAnsweredByUserIn(@Param("users") Collection<User> users);

    /**
     * All questions the student has answered, summed over their subjects
     * (practice answers and scored mock exams). {@code COALESCE} turns the
     * {@code null} that {@code SUM} gives for no rows into 0.
     */
    @Query("SELECT COALESCE(SUM(s.answered), 0) FROM SubjectStats s WHERE s.user = :user")
    long countAnsweredByUser(@Param("user") User user);

    /**
     * Adds one answer to the totals, letting the database do the adding
     * ({@code answered = answered + 1}) so two answers saved at the same time
     * can't overwrite each other.
     *
     * @return the number of rows updated: 0 means the row doesn't exist yet
     */
    @Modifying
    @Query("""
            UPDATE SubjectStats s
            SET s.answered = s.answered + 1,
                s.correct = s.correct + :correctIncrement,
                s.lastAnsweredAt = :answeredAt
            WHERE s.user = :user AND s.subject = :subject
            """)
    int addAnswer(@Param("user") User user,
                  @Param("subject") Subject subject,
                  @Param("correctIncrement") long correctIncrement,
                  @Param("answeredAt") LocalDateTime answeredAt);
}
