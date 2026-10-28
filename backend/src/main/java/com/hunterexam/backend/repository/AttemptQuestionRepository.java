package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.AttemptQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttemptQuestionRepository extends JpaRepository<AttemptQuestion, Long> {

    // Spring builds the query from the method name: WHERE attempt.id = ? AND question.id = ?
    Optional<AttemptQuestion> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
