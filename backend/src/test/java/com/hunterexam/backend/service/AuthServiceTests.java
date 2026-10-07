package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LoginRequest;
import com.hunterexam.backend.dto.RegisterRequest;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for register and login in {@link AuthService}.
 * The repository and JwtService are Mockito mocks; the password encoder is
 * the real BCrypt encoder, so the tests check real hashing.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    private static final String EMAIL = "student@example.test";
    private static final String PASSWORD = "correct-password";

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    private RegisterRequest registration() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Student Name");
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        return request;
    }

    /** A student already in the database, with the password stored as a BCrypt hash. */
    private User existingStudent() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(Role.STUDENT);
        return user;
    }

    private LoginRequest login(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    // ----- Register -----

    @Test
    void registerSavesNewStudentWithHashedPassword() {
        // Arrange: no user with this email exists yet, and save(...) hands
        // back the same object it was given (like a real save would)
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        User created = authService.register(registration());

        // Assert
        assertEquals(EMAIL, created.getEmail());
        assertEquals("Student Name", created.getFullName());
        assertEquals(Role.STUDENT, created.getRole(), "new users must never get another role");
        assertNotNull(created.getCreatedAt());

        // The password is stored as a BCrypt hash, never as plain text
        assertNotEquals(PASSWORD, created.getPassword());
        assertTrue(passwordEncoder.matches(PASSWORD, created.getPassword()));
    }

    @Test
    void registerWithAnEmailThatIsAlreadyUsedIsRejected() {
        // Arrange: the repository already has a user with this email
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(new User()));

        // Act + Assert
        assertThrows(RuntimeException.class, () -> authService.register(registration()));

        // findByEmail is called on purpose, so verifyNoInteractions can't be used.
        // The important part: no second account is saved.
        verify(userRepository, never()).save(any());
    }

    // ----- Login -----

    @Test
    void loginWithTheRightPasswordReturnsAToken() {
        // Arrange: the user exists, and the fake JwtService hands out a known token
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingStudent()));
        when(jwtService.generateToken(EMAIL, "STUDENT")).thenReturn("fake-jwt");

        // Act
        String token = authService.login(login(EMAIL, PASSWORD));

        // Assert: the token came from JwtService, created for this email and role
        assertEquals("fake-jwt", token);
    }

    @Test
    void loginWithTheWrongPasswordIsRejected() {
        // Arrange: the user exists, but the typed password is wrong
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingStudent()));

        // Act + Assert
        assertThrows(RuntimeException.class,
                () -> authService.login(login(EMAIL, "wrong-password")));

        // The important part: no token is ever created for a wrong password
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginWithAnUnknownEmailIsRejected() {
        // Arrange: no user has this email
        when(userRepository.findByEmail("nobody@example.test")).thenReturn(Optional.empty());

        // Act + Assert: assertThrows also hands back the exception, so we can read it
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> authService.login(login("nobody@example.test", PASSWORD)));

        // GlobalExceptionHandler picks 401 "Invalid email or password" by looking
        // for "invalid" or "password" in the message, so the message matters here
        assertEquals("Invalid email or password", error.getMessage());
        verifyNoInteractions(jwtService);
    }
}
