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
        // Swedish, like the app (#209). No app name in the text: the sender name
        // (MAIL_FROM) shows it, so a rename only changes that setting.
        message.setSubject("Återställ ditt lösenord");
        // A fragment keeps the token out of HTTP access logs and referrer headers.
        message.setText("Återställ ditt lösenord med den här länken:\n"
                + frontendUrl + "/reset-password#token=" + token
                + "\n\nLänken gäller i 30 minuter och kan bara användas en gång."
                + "\nOm du inte har bett om att återställa lösenordet kan du strunta i det här mejlet.");
        mailSender.send(message);
    }
}
