package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.UserResponse;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user profile operations.
 * <p>
 * This service retrieves the logged-in user's profile information
 * and allows the user to change their password.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * Fetches the profile information of the logged-in user.
     *
     * @param email user's email id
     * @return the user's profile details
     */
    public UserResponse getUserInfo(String email) {

         User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Invalid email"));

         return new UserResponse(
                 user.getId(),
                 user.getFullName(),
                 user.getEmail(),
                 user.getRole().name(),
                 user.getCreatedAt()
         );
    }


    /**
     * Changes the password of the logged-in user.
     * <p>
     * Verifies the current password before updating it with the new,
     * BCrypt-hashed password.
     *
     * @param email           user's email id
     * @param currentPassword the user's current password, for verification
     * @param newPassword     the new password to set
     */
    public void changePassword(String email, String currentPassword, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify the current password matches before allowing the change.
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {

            throw new RuntimeException("Current password is incorrect");
        }

        // Hash the new password before storing it — never store plain text passwords.
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
}
