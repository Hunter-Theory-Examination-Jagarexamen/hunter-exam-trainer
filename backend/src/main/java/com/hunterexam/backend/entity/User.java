package com.hunterexam.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.Date;

/**
 * Represents a registered user of the application.
 * <p>
 * Stores login credentials and profile information, and is linked to
 * the user's practice and mock exam results.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    // Stores the BCrypt-hashed password, never plain text.
    @Column(nullable = false)
    private String password;

    // Existing password accounts remain eligible; new Google-only accounts opt out.
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean passwordLoginEnabled = true;

    @Column(length = 64, unique = true)
    private String passwordResetTokenHash;

    private Instant passwordResetExpiresAt;

    // STUDENT or ADMIN. New users are registered as STUDENT (see AuthService).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
