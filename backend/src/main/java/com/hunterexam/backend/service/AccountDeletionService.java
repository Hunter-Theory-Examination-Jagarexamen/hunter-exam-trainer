package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.FeedbackRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Deletes a user and everything that identifies them (GDPR right to erasure, #194).
 * <p>
 * The deletion is one transaction: either everything goes, or nothing does.
 * Feedback is kept but de-linked, because it is useful to the course leaders
 * without the sender.
 */
@Service
public class AccountDeletionService {

    private final UserRepository userRepository;
    private final AttemptRepository attemptRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final SubjectStatsRepository subjectStatsRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountDeletionService(UserRepository userRepository,
                                  AttemptRepository attemptRepository,
                                  AttemptQuestionRepository attemptQuestionRepository,
                                  SubjectStatsRepository subjectStatsRepository,
                                  FeedbackRepository feedbackRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.attemptQuestionRepository = attemptQuestionRepository;
        this.subjectStatsRepository = subjectStatsRepository;
        this.feedbackRepository = feedbackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Deletes the authenticated user's own account. The password is required
     * as re-authentication. A wrong password throws {@link BadCredentialsException}
     * (→ 401 with the project's existing "Invalid email or password" handler, but
     * that message is misleading for delete; the controller overrides it — see below).
     */
    @Transactional
    public void deleteOwnAccount(String email, String password) {
        User user = userRepository.findByIdForUpdate(
                userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found"))
                    .getId())
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isPasswordLoginEnabled()
            || user.getPassword() == null
            || !passwordEncoder.matches(password, user.getPassword())) {
            // The generic handler turns BadCredentialsException into 401 "Invalid email or password".
            // That's fine as a shape, but the frontend shows a specific message for delete,
            // so the controller throws a dedicated exception instead — see UserController.
            throw new IllegalArgumentException("Wrong password");
        }

        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new ConflictException("You are the last admin and cannot delete your account.");
        }

        performDeletion(user);
    }

    /**
     * Admin endpoint: deletes a student. Admins can't be deleted through this
     * endpoint (they must delete themselves via {@link #deleteOwnAccount}).
     */
    @Transactional
    public void deleteStudentAsAdmin(Long targetId) {
        User target = userRepository.findByIdForUpdate(targetId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        if (target.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Admin accounts cannot be deleted through this endpoint.");
        }
        performDeletion(target);
    }

    /**
     * The actual deletion. Both public methods call this after their own guards.
     * Order matters: children before the parent, so we never rely on database
     * cascade behaviour (the entities don't declare cascade anyway).
     */
    private void performDeletion(User user) {
        Long userId = user.getId();
        // 1) All of the user's attempt questions (answers).
        List<Long> attemptIds = attemptRepository.findIdsByUser(userId);
        if (!attemptIds.isEmpty()) {
            attemptQuestionRepository.deleteByAttemptIds(attemptIds);
        }
        // 2) The attempts themselves (practice rounds + mock exams).
        attemptRepository.deleteByUser(userId);
        // 3) Per-subject running totals.
        subjectStatsRepository.deleteByUser(userId);
        // 4) Feedback is kept; drop only the link to the sender.
        feedbackRepository.detachFromSender(user);
        // 5) The user.
        userRepository.delete(user);
    }

}
