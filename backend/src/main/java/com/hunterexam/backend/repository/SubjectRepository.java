package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.repository.projection.SubjectQuestionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Every subject with its number of questions, ordered by subject id.
     * {@code LEFT JOIN} keeps subjects without questions (count 0); a plain
     * join would leave them out.
     */
    @Query("""
            SELECT new com.hunterexam.backend.repository.projection.SubjectQuestionCount(
                s.id, s.name, COUNT(q))
            FROM Subject s LEFT JOIN Question q ON q.subject = s
            GROUP BY s.id, s.name
            ORDER BY s.id
            """)
    List<SubjectQuestionCount> findQuestionCounts();
}
