package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findBySubjectId(Long subjectId);

    long countBySubjectId(Long subjectId);

    /**
     * Every question with its subject loaded in the same query, for random
     * practice (#151), which groups the whole bank by subject. Without the
     * {@code JOIN FETCH} each question would load its subject separately.
     */
    @Query("SELECT q FROM Question q JOIN FETCH q.subject")
    List<Question> findAllWithSubject();
}
