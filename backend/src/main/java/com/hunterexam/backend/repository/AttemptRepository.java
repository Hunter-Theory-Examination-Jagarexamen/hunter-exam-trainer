package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    // The user's running rounds of the given types: WHERE user = ? AND finishedAt IS NULL AND type IN (...)
    List<Attempt> findByUserAndFinishedAtIsNullAndTypeIn(User user, Collection<AttemptType> types);
}
