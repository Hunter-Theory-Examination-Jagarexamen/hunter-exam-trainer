package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    List<ExamResult> findByUserOrderByCompletedAtDesc(User user);
}
