package com.hunterexam.backend.entity;

/**
 * Roles that can be assigned to a user.
 * <p>
 * STUDENT is the default role assigned during registration.
 * ADMIN grants access to administrative endpoints under {@code /api/admin/**}.
 */
public enum Role {
    STUDENT,
    ADMIN
}