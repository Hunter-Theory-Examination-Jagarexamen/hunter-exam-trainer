package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LoginRequest;
import com.hunterexam.backend.dto.RegisterRequest;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    @Value("${google.login.enabled:false}")
    private boolean googleLoginEnabled;

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
     *
     * @param request registration details provided by the client
     * @return the newly created user
     */
    public User register(RegisterRequest request) {

        // Prevent multiple accounts from being registered with the same email.
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        // Store a BCrypt-hashed password instead of the original password.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // New users are registered as students by default.
        user.setRole(Role.STUDENT);
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

        if (request == null) {
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
                user.getRole().name()
        );
    }

    /**
     * Starts a guest session.
     * <p>
     * A shared guest account is created on the first request and reused
     * afterwards, returning a JWT so guests can use the application
     * without registering.
     *
     * @return JWT token for the guest account
     */
    public String guestLogin() {

        String guestEmail = "guest@hunterexam.local";

        User user = userRepository.findByEmail(guestEmail)
                .orElseGet(() -> {
                    User guest = new User();
                    guest.setFullName("Guest");
                    guest.setEmail(guestEmail);
                    guest.setPassword(passwordEncoder.encode(
                            UUID.randomUUID().toString()
                    ));
                    guest.setRole(Role.STUDENT);
                    guest.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(guest);
                });

        return jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }

    /**
     * Authenticates a user coming from Google OAuth2.
     * <p>
     * The first time a Google account is used, a matching user is created
     * automatically. A JWT is then returned like for a normal login.
     *
     * @param email email provided by Google
     * @param fullName display name provided by Google
     * @return JWT token for the OAuth2 user
     */
    public String oAuthLogin(String email, String fullName) {

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User oauthUser = new User();
                    oauthUser.setFullName(fullName);
                    oauthUser.setEmail(email);
                    oauthUser.setPassword(passwordEncoder.encode(
                            UUID.randomUUID().toString()
                    ));
                    oauthUser.setRole(Role.STUDENT);
                    oauthUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(oauthUser);
                });

        return jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }

    /**
     * Resets a user's password to a new generated value.
     * <p>
     * This development-friendly flow returns the new password directly so
     * it can be displayed to the user without requiring a mail server.
     *
     * @param email email of the account to reset
     * @return the generated password, or null when no account matches
     */
    public String forgotPassword(String email) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return null;
        }

        String newPassword = generateRandomPassword();

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return newPassword;
    }

    /**
     * @return true when Google login is configured and enabled
     */
    public boolean isGoogleLoginEnabled() {
        return googleLoginEnabled;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
