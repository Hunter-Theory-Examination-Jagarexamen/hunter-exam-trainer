package com.hunterexam.backend.dto;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Response containing the profile information of the
 * currently authenticated user.
 * <p>
 * Includes the user's ID, name, email, role, and
 * account creation date.
 */
@Getter
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public UserResponse(
            Long id,
            String fullName,
            String email,
            String role,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }
}
