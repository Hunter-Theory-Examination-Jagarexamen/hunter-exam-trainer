package com.hunterexam.backend.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetEmailServiceTests {
    @Test
    void sendsConfiguredFrontendLinkAndExpiryWithoutExternalEmail() {
        JavaMailSender sender = mock(JavaMailSender.class);
        new PasswordResetEmailService(sender, "no-reply@example.test", "https://frontend.example.test/")
                .sendPasswordReset("student@example.test", "test-token");
        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(message.capture());
        assertEquals("no-reply@example.test", message.getValue().getFrom());
        assertArrayEquals(new String[]{"student@example.test"}, message.getValue().getTo());
        assertTrue(message.getValue().getText().contains("https://frontend.example.test/reset-password#token=test-token"));
        assertTrue(message.getValue().getText().contains("30 minutes"));
        assertTrue(message.getValue().getText().contains("only be used once"));
    }
}
