package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Every subject, oldest first — the order the statistics list them in, so
     * a student and a course leader always see the same rows in the same order.
     */
    List<Subject> findAllByOrderById();
}
