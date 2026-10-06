package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;

    public PasswordResetService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            PasswordResetEmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailForPasswordReset(email)
                .filter(this::canResetPassword)
                .ifPresent(user -> {
                    byte[] bytes = new byte[32];
                    random.nextBytes(bytes);
                    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                    // Replacing these fields invalidates any previous outstanding link.
                    user.setPasswordResetTokenHash(hashToken(token));
                    user.setPasswordResetExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
                    try {
                        emailService.sendPasswordReset(user.getEmail(), token);
                    } catch (MailException ex) {
                        clearToken(user);
                        // SMTP failures must not reveal account existence or sensitive mail contents.
                        log.warn("Password reset email delivery failed; check SMTP configuration and availability.");
                    }
                    userRepository.save(user);
                });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw invalidToken();
        }
        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        // BCrypt supports at most 72 UTF-8 bytes. Reject longer input rather than truncating it.
        if (newPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes");
        }
        User user = userRepository.findByPasswordResetTokenHash(hashToken(token))
                .orElseThrow(this::invalidToken);
        if (!canResetPassword(user) || !hashToken(token).equals(user.getPasswordResetTokenHash())
                || user.getPasswordResetExpiresAt() == null
                || !user.getPasswordResetExpiresAt().isAfter(Instant.now())) {
            throw invalidToken();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        clearToken(user);
        // The row lock and transaction cover both password update and token consumption.
        userRepository.save(user);
    }

    private boolean canResetPassword(User user) {
        return user.isPasswordLoginEnabled();
    }

    private void clearToken(User user) {
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetExpiresAt(null);
    }

    private IllegalArgumentException invalidToken() {
        return new IllegalArgumentException("Reset link is invalid, expired, or already used. Please request a new reset link.");
    }

    static String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
