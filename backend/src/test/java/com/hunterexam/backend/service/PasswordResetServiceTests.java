package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LoginRequest;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTests {
    @Mock UserRepository users;
    @Mock PasswordResetEmailService emails;
    @Mock JwtService jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private PasswordResetService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(users, encoder, emails);
        user = new User();
        user.setEmail("student@example.test");
        user.setRole(Role.STUDENT);
        user.setPassword(encoder.encode("old-password"));
    }

    private String requestToken() {
        when(users.findByEmailForPasswordReset(user.getEmail())).thenReturn(Optional.of(user));
        service.requestReset(user.getEmail());
        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emails).sendPasswordReset(eq(user.getEmail()), token.capture());
        return token.getValue();
    }

    @Test
    void creates256BitTokenStoresOnlyHashAndDoesNotChangePassword() {
        String oldPassword = user.getPassword();
        Instant before = Instant.now();
        String token = requestToken();
        assertEquals(32, Base64.getUrlDecoder().decode(token).length);
        assertTrue(token.matches("[A-Za-z0-9_-]{43}"));
        assertEquals(64, user.getPasswordResetTokenHash().length());
        assertEquals(PasswordResetService.hashToken(token), user.getPasswordResetTokenHash());
        assertNotEquals(token, user.getPasswordResetTokenHash());
        assertEquals(oldPassword, user.getPassword());
        assertTrue(user.getPasswordResetExpiresAt().isAfter(before.plusSeconds(1799)));
        assertTrue(user.getPasswordResetExpiresAt().isBefore(Instant.now().plusSeconds(1801)));
    }

    @Test
    void unknownGoogleOnlyAndGuestAccountsSendNoEmail() {
        service.requestReset("unknown@example.test");
        user.setPasswordLoginEnabled(false);
        when(users.findByEmailForPasswordReset(user.getEmail())).thenReturn(Optional.of(user));
        service.requestReset(user.getEmail());
        user.setPasswordLoginEnabled(true);
        user.setEmail("guest@hunterexam.local");
        when(users.findByEmailForPasswordReset(user.getEmail())).thenReturn(Optional.of(user));
        service.requestReset(user.getEmail());
        verifyNoInteractions(emails);
        verify(users, never()).save(any());
    }

    @Test
    void replacementGeneratesDifferentTokenAndInvalidatesOldOne() {
        String first = requestToken();
        String firstHash = user.getPasswordResetTokenHash();
        reset(emails);
        String second = requestToken();
        assertNotEquals(first, second);
        assertNotEquals(firstHash, user.getPasswordResetTokenHash());
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(first, "new-password"));
    }

    @Test
    void validTokenEncodesPasswordConsumesTokenAndAllowsLogin() {
        String token = requestToken();
        String hash = user.getPasswordResetTokenHash();
        when(users.findByPasswordResetTokenHash(hash)).thenAnswer(invocation ->
                hash.equals(user.getPasswordResetTokenHash()) ? Optional.of(user) : Optional.empty());
        service.resetPassword(token, "new-password");
        assertNotEquals("new-password", user.getPassword());
        assertTrue(encoder.matches("new-password", user.getPassword()));
        assertFalse(encoder.matches("old-password", user.getPassword()));
        assertNull(user.getPasswordResetTokenHash());
        assertNull(user.getPasswordResetExpiresAt());
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(token, "another-password"));
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwt.generateToken(user.getEmail(), "STUDENT")).thenReturn("login-jwt");
        LoginRequest login = new LoginRequest();
        login.setEmail(user.getEmail());
        login.setPassword("new-password");
        assertEquals("login-jwt", new AuthService(users, encoder, jwt).login(login));
    }

    @Test
    void expiredTokenCannotChangePassword() {
        String token = requestToken();
        user.setPasswordResetExpiresAt(Instant.now().minusSeconds(1));
        String oldPassword = user.getPassword();
        when(users.findByPasswordResetTokenHash(user.getPasswordResetTokenHash())).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(token, "new-password"));
        assertEquals(oldPassword, user.getPassword());
    }

    @Test
    void invalidAndAlreadyUsedTokensAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword("invalid", "new-password"));
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword("a".repeat(43), "new-password"));
        verify(users, never()).save(any());
    }

    @Test
    void invalidPasswordsAreRejectedBeforeConsumingToken() {
        for (String password : new String[]{"short", " ".repeat(8), "a".repeat(73), "é".repeat(37)}) {
            assertThrows(IllegalArgumentException.class, () -> service.resetPassword("a".repeat(43), password));
        }
        verifyNoInteractions(users);
    }

    @Test
    void smtpFailureDoesNotEscapeOrChangePasswordAndClearsToken() {
        when(users.findByEmailForPasswordReset(user.getEmail())).thenReturn(Optional.of(user));
        doThrow(new MailSendException("Simulated delivery failure")).when(emails).sendPasswordReset(anyString(), anyString());
        String oldPassword = user.getPassword();
        assertDoesNotThrow(() -> service.requestReset(user.getEmail()));
        assertNull(user.getPasswordResetTokenHash());
        assertNull(user.getPasswordResetExpiresAt());
        assertEquals(oldPassword, user.getPassword());
    }

    @Test
    void newGoogleAccountsAreMarkedButExistingPasswordAccountsStayEligible() {
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        new AuthService(users, encoder, jwt).oAuthLogin("google@example.test", "Google User");
        ArgumentCaptor<User> created = ArgumentCaptor.forClass(User.class);
        verify(users).save(created.capture());
        assertFalse(created.getValue().isPasswordLoginEnabled());
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        new AuthService(users, encoder, jwt).oAuthLogin(user.getEmail(), "Student");
        assertTrue(user.isPasswordLoginEnabled());
    }
}
