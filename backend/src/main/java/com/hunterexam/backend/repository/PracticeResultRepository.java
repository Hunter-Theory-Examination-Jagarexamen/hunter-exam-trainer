package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PracticeResultRepository
        extends JpaRepository<PracticeResult, Long> {

    List<PracticeResult> findByUserAndSubject(User user, Subject subject);

    List<PracticeResult> findByUser(User user);

    List<PracticeResult> findByUserAndCompletedAtAfter(User user, LocalDateTime since);
}
