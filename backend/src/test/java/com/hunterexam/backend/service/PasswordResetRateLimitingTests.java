package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.TooManyRequestsException;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifies the forgot-password limit end to end through {@link PasswordResetService}
 * with a real limiter and a controlled clock. The email service is always mocked,
 * so no email can leave the test run.
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetRateLimitingTests {

    private static final String EMAIL = "student@example.test";
    private static final String UNKNOWN_EMAIL = "unknown@example.test";
    private static final long WINDOW_SECONDS = 60;
    private static final String REJECTED_MESSAGE = "Too many password reset requests. Please try again later.";

    @Mock UserRepository users;
    @Mock PasswordResetEmailService emails;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final MutableClock clock = new MutableClock();
    private PasswordResetService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(users, encoder, emails,
                new PasswordResetRateLimiter(WINDOW_SECONDS, 1000, clock));
        user = new User();
        user.setEmail(EMAIL);
        user.setRole(Role.STUDENT);
        user.setPassword(encoder.encode("old-password"));
    }

    private String registeredAndRequestedToken() {
        when(users.findByEmailForPasswordReset(EMAIL)).thenReturn(Optional.of(user));
        service.requestReset(EMAIL);
        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emails).sendPasswordReset(eq(EMAIL), token.capture());
        return token.getValue();
    }

    @Test
    void firstRequestForARegisteredAddressSendsExactlyOneEmail() {
        when(users.findByEmailForPasswordReset(EMAIL)).thenReturn(Optional.of(user));

        service.requestReset(EMAIL);

        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emails).sendPasswordReset(eq(EMAIL), token.capture());
        assertEquals(PasswordResetService.hashToken(token.getValue()), user.getPasswordResetTokenHash());
        assertNotNull(user.getPasswordResetExpiresAt());
    }

    @Test
    void repeatedRequestInsideTheWindowSendsNoSecondEmailAndSkipsTheAccountLookup() {
        when(users.findByEmailForPasswordReset(EMAIL)).thenReturn(Optional.of(user));
        service.requestReset(EMAIL);

        TooManyRequestsException rejected =
                assertThrows(TooManyRequestsException.class, () -> service.requestReset(EMAIL));

        assertEquals(REJECTED_MESSAGE, rejected.getMessage());
        verify(emails, times(1)).sendPasswordReset(anyString(), anyString());
        // The limit is applied before the account is looked up at all.
        verify(users, times(1)).findByEmailForPasswordReset(EMAIL);
    }

    @Test
    void rejectedRequestDoesNotCreateOrReplaceAResetToken() {
        when(users.findByEmailForPasswordReset(EMAIL)).thenReturn(Optional.of(user));
        service.requestReset(EMAIL);
        String hashAfterFirstRequest = user.getPasswordResetTokenHash();
        Instant expiryAfterFirstRequest = user.getPasswordResetExpiresAt();

        assertThrows(TooManyRequestsException.class, () -> service.requestReset(EMAIL));

        assertEquals(hashAfterFirstRequest, user.getPasswordResetTokenHash());
        assertEquals(expiryAfterFirstRequest, user.getPasswordResetExpiresAt());
        verify(users, times(1)).save(any());
    }

    @Test
    void registeredAndUnregisteredAddressesShareOneLimitAndOneMessage() {
        when(users.findByEmailForPasswordReset(EMAIL)).thenReturn(Optional.of(user));
        when(users.findByEmailForPasswordReset(UNKNOWN_EMAIL)).thenReturn(Optional.empty());

        service.requestReset(EMAIL);
        String registeredMessage =
                assertThrows(TooManyRequestsException.class, () -> service.requestReset(EMAIL)).getMessage();

        service.requestReset(UNKNOWN_EMAIL);
        String unregisteredMessage =
                assertThrows(TooManyRequestsException.class, () -> service.requestReset(UNKNOWN_EMAIL)).getMessage();

        assertEquals(REJECTED_MESSAGE, registeredMessage);
        assertEquals(registeredMessage, unregisteredMessage);
        verify(emails, times(1)).sendPasswordReset(anyString(), anyString());
    }

    @Test
    void casingVariantsDoNotOpenASecondSlot() {
        when(users.findByEmailForPasswordReset(EMAIL)).thenReturn(Optional.of(user));
        service.requestReset(EMAIL);

        for (String variant : new String[]{"Student@Example.test", "STUDENT@EXAMPLE.TEST",
                " student@example.test "}) {
            TooManyRequestsException rejected =
                    assertThrows(TooManyRequestsException.class, () -> service.requestReset(variant), variant);
            assertEquals(REJECTED_MESSAGE, rejected.getMessage());
        }
        verify(emails, times(1)).sendPasswordReset(anyString(), anyString());
    }

    @Test
    void addressBecomesEligibleAgainAfterTheWindowExpires() {
        when(users.findByEmailForPasswordReset(EMAIL)).thenReturn(Optional.of(user));

        service.requestReset(EMAIL);
        clock.advanceSeconds(59);
        assertThrows(TooManyRequestsException.class, () -> service.requestReset(EMAIL));
        verify(emails, times(1)).sendPasswordReset(anyString(), anyString());

        clock.advanceSeconds(1);
        assertDoesNotThrow(() -> service.requestReset(EMAIL));
        verify(emails, times(2)).sendPasswordReset(eq(EMAIL), anyString());
    }

    @Test
    void concurrentRequestsSendExactlyOneEmail() throws Exception {
        when(users.findByEmailForPasswordReset(EMAIL)).thenReturn(Optional.of(user));
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger allowed = new AtomicInteger();
        AtomicInteger limited = new AtomicInteger();

        try (var executor = Executors.newFixedThreadPool(threads)) {
            Callable<Boolean> task = () -> {
                assertTrue(start.await(10, TimeUnit.SECONDS));
                try {
                    service.requestReset(EMAIL);
                    return true;
                } catch (TooManyRequestsException ex) {
                    return false;
                }
            };
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(task));
            }
            start.countDown();
            for (Future<Boolean> future : futures) {
                if (future.get(15, TimeUnit.SECONDS)) {
                    allowed.incrementAndGet();
                } else {
                    limited.incrementAndGet();
                }
            }
        }

        assertEquals(1, allowed.get());
        assertEquals(threads - 1, limited.get());
        verify(emails, times(1)).sendPasswordReset(eq(EMAIL), anyString());
        verify(users, times(1)).save(any());
    }

    @Test
    void otherAddressesKeepTheirOwnSlotWhileOneIsLimited() {
        when(users.findByEmailForPasswordReset(EMAIL)).thenReturn(Optional.of(user));
        when(users.findByEmailForPasswordReset("colleague@example.test")).thenReturn(Optional.empty());

        service.requestReset(EMAIL);

        assertThrows(TooManyRequestsException.class, () -> service.requestReset(EMAIL));
        assertDoesNotThrow(() -> service.requestReset("colleague@example.test"));
    }

    @Test
    void resetFlowStillWorksForAnAllowedRequest() {
        String token = registeredAndRequestedToken();
        assertThrows(TooManyRequestsException.class, () -> service.requestReset(EMAIL));

        String hash = user.getPasswordResetTokenHash();
        when(users.findByPasswordResetTokenHash(hash)).thenReturn(Optional.of(user));

        service.resetPassword(token, "new-password");

        assertTrue(encoder.matches("new-password", user.getPassword()));
        assertNull(user.getPasswordResetTokenHash());
        assertNull(user.getPasswordResetExpiresAt());
    }
}
