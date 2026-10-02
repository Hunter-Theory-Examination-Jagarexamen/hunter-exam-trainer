package com.hunterexam.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;

    public PasswordResetEmailService(JavaMailSender mailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.frontend.url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    public void sendPasswordReset(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Hunter Exam Trainer: reset your password");
        // A fragment keeps the token out of HTTP access logs and referrer headers.
        message.setText("Reset your password using this link:\n"
                + frontendUrl + "/reset-password#token=" + token
                + "\n\nThis link expires in 30 minutes and can only be used once."
                + "\nIf you did not request this, you can ignore this email.");
        mailSender.send(message);
    }
}
