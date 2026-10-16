package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@DataJpaTest(showSql = false)
@Import({PasswordResetService.class, PasswordResetPersistenceTests.EncoderConfiguration.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PasswordResetPersistenceTests {
    @Autowired UserRepository users;
    @Autowired PasswordResetService service;
    @Autowired PasswordEncoder encoder;
    @Autowired PlatformTransactionManager transactionManager;
    @MockitoBean PasswordResetEmailService emails;
    private final String token = "a".repeat(43);
    private Long userId;

    @TestConfiguration
    static class EncoderConfiguration {
        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    }

    @BeforeEach
    void setUp() {
        users.deleteAll();
        User user = new User();
        user.setEmail("student@example.test");
        user.setFullName("Student");
        user.setRole(Role.STUDENT);
        user.setCreatedAt(LocalDateTime.now());
        user.setPassword(encoder.encode("old-password"));
        user.setPasswordResetTokenHash(PasswordResetService.hashToken(token));
        user.setPasswordResetExpiresAt(Instant.now().plusSeconds(1800));
        userId = users.saveAndFlush(user).getId();
    }

    @Test
    void commitsEncodedPasswordAndTokenConsumptionTogether() {
        service.resetPassword(token, "new-password");
        User saved = users.findById(userId).orElseThrow();
        assertTrue(encoder.matches("new-password", saved.getPassword()));
        assertNull(saved.getPasswordResetTokenHash());
        assertNull(saved.getPasswordResetExpiresAt());
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(token, "other-password"));
    }

    @Test
    void expiredTokenLeavesPersistedPasswordUnchanged() {
        User user = users.findById(userId).orElseThrow();
        user.setPasswordResetExpiresAt(Instant.now().minusSeconds(1));
        users.saveAndFlush(user);
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(token, "new-password"));
        assertTrue(encoder.matches("old-password", users.findById(userId).orElseThrow().getPassword()));
    }

    @Test
    void requestsPersistOnlyLatestHashAndKeepPasswordUntilReset() {
        var sentTokens = new ArrayList<String>();
        doAnswer(invocation -> {
            sentTokens.add(invocation.getArgument(1));
            return null;
        }).when(emails).sendPasswordReset(anyString(), anyString());
        service.requestReset("student@example.test");
        service.requestReset("student@example.test");
        assertEquals(2, sentTokens.size());
        User saved = users.findById(userId).orElseThrow();
        assertEquals(PasswordResetService.hashToken(sentTokens.get(1)), saved.getPasswordResetTokenHash());
        assertNotEquals(sentTokens.get(1), saved.getPasswordResetTokenHash());
        assertTrue(encoder.matches("old-password", saved.getPassword()));
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(sentTokens.get(0), "new-password"));
        service.resetPassword(sentTokens.get(1), "new-password");
        assertTrue(encoder.matches("new-password", users.findById(userId).orElseThrow().getPassword()));
    }

    @Test
    void transactionRollbackPreservesBothPasswordAndToken() {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            service.resetPassword(token, "new-password");
            users.flush();
            throw new IllegalStateException("Simulated transaction failure");
        }));
        User saved = users.findById(userId).orElseThrow();
        assertTrue(encoder.matches("old-password", saved.getPassword()));
        assertEquals(PasswordResetService.hashToken(token), saved.getPasswordResetTokenHash());
        assertNotNull(saved.getPasswordResetExpiresAt());
        service.resetPassword(token, "new-password");
    }

    @Test
    void twoConcurrentResetsHaveExactlyOneWinner() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var task = (java.util.concurrent.Callable<Boolean>) () -> {
                assertTrue(start.await(10, TimeUnit.SECONDS));
                try {
                    service.resetPassword(token, "new-password");
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        }
        User saved = users.findById(userId).orElseThrow();
        assertTrue(encoder.matches("new-password", saved.getPassword()));
        assertNull(saved.getPasswordResetTokenHash());
    }
}
