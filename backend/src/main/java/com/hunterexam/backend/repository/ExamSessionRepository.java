package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession,Long> {
}
