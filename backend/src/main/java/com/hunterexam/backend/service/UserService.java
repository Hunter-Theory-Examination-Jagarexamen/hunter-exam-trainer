package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.UserResponse;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public UserResponse getUserInfo(String email) {

         User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Invalid email"));

         return new UserResponse(
                 user.getId(),
                 user.getFullName(),
                 user.getEmail(),
                 user.getRole(),
                 user.getCreatedAt()
         );
    }

    public void changePassword(String email, String currentPassword, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {

            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
}
