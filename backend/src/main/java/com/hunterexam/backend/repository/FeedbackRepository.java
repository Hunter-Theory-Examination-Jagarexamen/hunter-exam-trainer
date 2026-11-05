package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Feedback;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /** How many items the user has sent since the given time (for the daily limit). */
    long countBySenderAndCreatedAtAfter(User sender, LocalDateTime since);

    /** Items not yet marked as handled (for the admin start page). */
    long countByHandledFalse();
}
