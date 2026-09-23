package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findBySubjectId(Long subjectId);

    long countBySubjectId(Long subjectId);
}
