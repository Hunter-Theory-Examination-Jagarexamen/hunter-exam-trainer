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

    @Query("""
            SELECT s FROM SubjectStats s
            JOIN FETCH s.subject
            WHERE s.user.id = :userId AND s.answered > 0
            ORDER BY s.subject.id
            """)
    List<SubjectStats> findAnsweredByUser(@Param("userId") Long userId);

    @Query("""
            SELECT s FROM SubjectStats s
            JOIN FETCH s.subject
            WHERE s.user IN :users AND s.answered > 0
            ORDER BY s.subject.id
            """)
    List<SubjectStats> findAnsweredByUserIn(@Param("users") Collection<User> users);

    @Query("SELECT COALESCE(SUM(s.answered), 0) FROM SubjectStats s WHERE s.user = :user")
    long countAnsweredByUser(@Param("user") User user);

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

    @Modifying
    @Query("delete from SubjectStats s where s.user.id = :userId")
    int deleteByUser(@Param("userId") Long userId);
}
