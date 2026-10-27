package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SubjectStatsRepository extends JpaRepository<SubjectStats, Long> {

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
