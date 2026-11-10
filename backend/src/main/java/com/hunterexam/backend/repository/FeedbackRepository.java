package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Feedback;
import com.hunterexam.backend.entity.FeedbackType;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /** How many items the user has sent since the given time (for the daily limit). */
    long countBySenderAndCreatedAtAfter(User sender, LocalDateTime since);

    /** Items not yet marked as handled (for the admin start page). */
    long countByHandledFalse();

    /**
     * Admin list, newest first. A {@code null} filter means "any": e.g. type
     * {@code null} and handled {@code false} gives every unhandled item.
     */
    @Query("""
            select f from Feedback f
            where (:type is null or f.type = :type)
              and (:handled is null or f.handled = :handled)
            order by f.createdAt desc
            """)
    List<Feedback> findForAdmin(@Param("type") FeedbackType type, @Param("handled") Boolean handled);


    @Modifying
    @Query("update Feedback f set f.sender = null where f.sender = :user")
    int detachFromSender(@Param("user") User user);
}
