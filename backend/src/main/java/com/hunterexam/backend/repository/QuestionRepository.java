package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.repository.projection.SubjectQuestionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findBySubjectId(Long subjectId);

    long countBySubjectId(Long subjectId);

    /**
     * Questions per subject, counted by the database: the denominator of the
     * per-subject mastery percentage. Subjects without questions are left out
     * (the services treat a missing entry as 0 questions).
     */
    @Query("""
            SELECT new com.hunterexam.backend.repository.projection.SubjectQuestionCount(
                q.subject.id, COUNT(q))
            FROM Question q
            GROUP BY q.subject.id
            """)
    List<SubjectQuestionCount> countGroupedBySubject();

    /**
     * Every question with its subject loaded in the same query, for random
     * practice (#151), which groups the whole bank by subject. Without the
     * {@code JOIN FETCH} each question would load its subject separately.
     */
    @Query("SELECT q FROM Question q JOIN FETCH q.subject")
    List<Question> findAllWithSubject();
}
