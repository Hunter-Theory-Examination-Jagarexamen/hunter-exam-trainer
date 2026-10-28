package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.AttemptQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptQuestionRepository extends JpaRepository<AttemptQuestion, Long> {
}
