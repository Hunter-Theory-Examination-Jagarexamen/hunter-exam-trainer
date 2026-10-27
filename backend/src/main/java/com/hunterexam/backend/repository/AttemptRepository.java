package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
}
