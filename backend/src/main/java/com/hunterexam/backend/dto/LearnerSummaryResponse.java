package com.hunterexam.backend.dto;

import com.hunterexam.backend.entity.Role;

/** One row in the admin's list of users: just enough to show the name and group it. */
public record LearnerSummaryResponse(
        Long userId,
        String fullName,
        Role role
) {}
