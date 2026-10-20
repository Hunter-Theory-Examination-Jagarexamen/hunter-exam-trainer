package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.DataInitializer;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.service.PasswordResetEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Drives the real forgot-password endpoint (controller, service, limiter, exception
 * handling) against the H2 test database.
 * <p>
 * The address bookkeeping lives in a singleton limiter, so every test uses its own
 * addresses instead of relying on test order. Email delivery is mocked, so no email
 * can leave the test run.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ForgotPasswordRateLimitHttpTests {

    private static final String REJECTED_BODY =
            "{\"message\":\"Too many password reset requests. Please try again later.\"}";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    @MockitoBean DataInitializer dataInitializer;
    @MockitoBean PasswordResetEmailService emails;

    private record Answer(int status, String body) {
    }

    @BeforeEach
    void setUp() {
        users.deleteAll();
    }

    private void register(String email) {
        User user = new User();
        user.setFullName("Rate Limit Student");
        user.setEmail(email);
        user.setPassword(encoder.encode("old-password"));
        user.setRole(Role.STUDENT);
        user.setCreatedAt(LocalDateTime.now());
        users.save(user);
    }

    private Answer forgotPassword(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andReturn();
        return new Answer(result.getResponse().getStatus(), result.getResponse().getContentAsString());
    }

    private Optional<String> storedResetTokenHash(String email) {
        return users.findByEmail(email).map(User::getPasswordResetTokenHash);
    }

    @Test
    void registeredAndUnregisteredAddressesGetTheSameStatusesAndBodies() throws Exception {
        register("registered@example.test");

        Answer registeredFirst = forgotPassword("registered@example.test");
        Answer unknownFirst = forgotPassword("unregistered@example.test");
        assertEquals(200, registeredFirst.status());
        assertEquals(registeredFirst.body(), unknownFirst.body());

        Answer registeredSecond = forgotPassword("registered@example.test");
        Answer unknownSecond = forgotPassword("unregistered@example.test");
        assertEquals(429, registeredSecond.status());
        assertEquals(429, unknownSecond.status());
        assertEquals(registeredSecond.body(), unknownSecond.body());
        assertEquals(REJECTED_BODY, registeredSecond.body());

        // Only the registered address produced an email; the limit did not depend on that.
        ArgumentCaptor<String> recipients = ArgumentCaptor.forClass(String.class);
        verify(emails, times(1)).sendPasswordReset(recipients.capture(), anyString());
        assertEquals("registered@example.test", recipients.getValue());
    }

    @Test
    void casingVariantsShareTheSlotOfTheRegisteredAddress() throws Exception {
        register("cased@example.test");

        assertEquals(200, forgotPassword("cased@example.test").status());
        assertEquals(429, forgotPassword("Cased@Example.test").status());
        assertEquals(429, forgotPassword("CASED@EXAMPLE.TEST").status());

        verify(emails, times(1)).sendPasswordReset(eq("cased@example.test"), anyString());
    }

    @Test
    void blockedRequestSendsNoEmailAndReplacesNoResetToken() throws Exception {
        register("token@example.test");

        assertEquals(200, forgotPassword("token@example.test").status());
        Optional<String> hashAfterFirst = storedResetTokenHash("token@example.test");
        assertTrue(hashAfterFirst.isPresent());

        assertEquals(429, forgotPassword("token@example.test").status());
        assertEquals(hashAfterFirst, storedResetTokenHash("token@example.test"));
        verify(emails, times(1)).sendPasswordReset(anyString(), anyString());
    }

    @Test
    void addressesOutsideTheLimitedOneAreUnaffected() throws Exception {
        register("first-scope@example.test");

        assertEquals(200, forgotPassword("first-scope@example.test").status());
        assertEquals(429, forgotPassword("first-scope@example.test").status());
        // A different address still has its own slot: the limit is per address.
        assertEquals(200, forgotPassword("second-scope@example.test").status());
    }
}
