package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    List<Attempt> findByUserAndFinishedAtIsNullAndTypeIn(User user, Collection<AttemptType> types);

    @Query("select a.id from Attempt a where a.user.id = :userId")
    List<Long> findIdsByUser(@Param("userId") Long userId);

    @Modifying
    @Query("delete from Attempt a where a.user.id = :userId")
    int deleteByUser(@Param("userId") Long userId);

}
