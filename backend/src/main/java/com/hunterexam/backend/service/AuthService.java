package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LoginRequest;
import com.hunterexam.backend.dto.RegisterRequest;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;



/**
 * Handles user registration and login.
 * <p>
 * This service contains the authentication-related business logic,
 * including password hashing, credential validation, and JWT generation.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user.
     * <p>
     * Checks that the email is not already registered, hashes the password
     * before storing it, assigns the default STUDENT role, and saves the user.
     * <p>
     * @param request registration details provided by the client
     * @return the newly created user
     */
    public User register(RegisterRequest request) {

        // Prevent multiple accounts from being registered with the same email.
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        // Store a BCrypt-hashed password instead of the original password.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // New users are registered as students by default.
        user.setRole("STUDENT");
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Authenticates an existing user and generates a JWT token.
     * <p>
     * The supplied email and password are checked against the stored
     * user credentials. A JWT containing the user's email and role is
     * returned when authentication is successful.
     *
     * @param request login credentials provided by the client
     * @return JWT token for the authenticated user
     */
    public String login(LoginRequest request) {

        if(request == null) {
            throw new RuntimeException("Login request cannot be null");
        }

        // Find the user using the email provided during login.
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Compare the entered password with the stored BCrypt hash.
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            throw new RuntimeException("Invalid Email or Password");
        }

        // Generate a JWT that the frontend uses for authenticated requests.
        return jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );
    }
}
