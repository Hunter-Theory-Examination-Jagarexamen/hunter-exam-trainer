package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.service.AuthService;
import com.hunterexam.backend.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class PasswordResetControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean AuthService auth;
    @MockitoBean PasswordResetService resets;
    @MockitoBean OAuth2LoginSuccessHandler loginHandler;

    @Test
    void everyValidEmailReceivesIdenticalGenericResponseWithoutAuthentication() throws Exception {
        for (String email : new String[]{"student@example.test", "unknown@example.test", "google@example.test"}) {
            mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + email + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"message\":\"If an eligible account exists for that email, a password reset link has been sent.\"}"))
                    .andExpect(jsonPath("$.newPassword").doesNotExist());
            verify(resets).requestReset(email);
        }
    }

    @Test
    void rejectsMissingBlankAndMalformedEmail() throws Exception {
        for (String body : new String[]{"{}", "{\"email\":\"\"}", "{\"email\":\"not-an-email\"}"}) {
            mvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
        }
        verifyNoInteractions(resets);
    }

    @Test
    void validResetReturnsSuccess() throws Exception {
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + "a".repeat(43) + "\",\"newPassword\":\"new-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Password reset successfully. You can now log in."));
        verify(resets).resetPassword("a".repeat(43), "new-password");
    }

    @Test
    void invalidExpiredOrUsedTokenReturnsClearBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Reset link is invalid, expired, or already used. Please request a new reset link."))
                .when(resets).resetPassword("a".repeat(43), "new-password");
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + "a".repeat(43) + "\",\"newPassword\":\"new-password\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Reset link is invalid, expired, or already used. Please request a new reset link."));
    }

    @Test
    void invalidResetInputNeverCallsService() throws Exception {
        for (String body : new String[]{"{}", "{\"token\":\"invalid\",\"newPassword\":\"new-password\"}",
                "{\"token\":\"" + "a".repeat(43) + "\",\"newPassword\":\"short\"}"}) {
            mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
        }
        verifyNoInteractions(resets);
    }

    @Test
    void malformedJsonAndMissingBodyReturnSafeValidationError() throws Exception {
        for (String endpoint : new String[]{"/api/auth/forgot-password", "/api/auth/reset-password"}) {
            for (String body : new String[]{"", "{\"newPassword\":malformed-secret}"}) {
                mvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(body))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().json("{\"message\":\"Please provide a valid JSON request body\"}"));
            }
        }
        verifyNoInteractions(resets);
    }
}
