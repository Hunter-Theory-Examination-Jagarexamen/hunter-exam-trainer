package com.hunterexam.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

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

    // Student-controlled sharing with course leaders (#125). Both default to
    // false (private by default): the columnDefinition makes ddl-auto=update
    // fill existing rows with false, and the field initialiser does the same
    // for new accounts, so old and new students behave identically.
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean shareSubjectStatistics = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean shareMockExamResults = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
