package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    List<Attempt> findByUserAndFinishedAtIsNullAndTypeIn(User user, Collection<AttemptType> types);

    /**
     * Closes the attempt if it is still open: sets {@code finishedAt} only
     * when it is still {@code null}, and reports whether this call was the
     * one that closed it ({@code 1}) or found it already closed ({@code 0}).
     * <p>
     * The conditional update is the exam-ending latch (#187): of several
     * concurrent endings of the same exam (abandon, late submit, the next
     * exam's cleanup) exactly one gets {@code 1} back, and only that one
     * counts the answers into the statistics. Because the update and the
     * counting happen in the same transaction, no answer can be counted
     * twice, and an exam already closed by a scored submit can't be counted
     * again through an unscored ending.
     */
    @Modifying
    @Query("""
            UPDATE Attempt a
            SET a.finishedAt = :finishedAt
            WHERE a.id = :id AND a.finishedAt IS NULL
            """)
    int finishIfOpen(@Param("id") Long id, @Param("finishedAt") LocalDateTime finishedAt);

    @Query("select a.id from Attempt a where a.user.id = :userId")
    List<Long> findIdsByUser(@Param("userId") Long userId);

    @Modifying
    @Query("delete from Attempt a where a.user.id = :userId")
    int deleteByUser(@Param("userId") Long userId);

}
