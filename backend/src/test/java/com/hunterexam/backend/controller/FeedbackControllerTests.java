package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.dto.FeedbackRequest;
import com.hunterexam.backend.entity.FeedbackType;
import com.hunterexam.backend.exception.TooManyRequestsException;
import com.hunterexam.backend.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedbackController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class FeedbackControllerTests {

    private static final String EMAIL = "student@example.test";

    @Autowired MockMvc mvc;
    @MockitoBean FeedbackService service;
    @MockitoBean OAuth2LoginSuccessHandler loginHandler;

    private MockHttpServletRequestBuilder postAsStudent(String body) {
        return post("/api/feedback")
                .with(jwt().jwt(token -> token.subject(EMAIL))
                        .authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    @Test
    void anonymousCannotSendFeedback() throws Exception {
        mvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"APP\", \"text\": \"Hej\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void studentSendsAQuestionReport() throws Exception {
        mvc.perform(postAsStudent("{\"type\": \"QUESTION\", \"text\": \"Fel svar\", \"questionId\": 12}"))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        ArgumentCaptor<FeedbackRequest> sent = ArgumentCaptor.forClass(FeedbackRequest.class);
        verify(service).send(eq(EMAIL), sent.capture());
        assertEquals(FeedbackType.QUESTION, sent.getValue().getType());
        assertEquals("Fel svar", sent.getValue().getText());
        assertEquals(12L, sent.getValue().getQuestionId());
    }

    @Test
    void missingTypeOrBlankTextGives400() throws Exception {
        mvc.perform(postAsStudent("{\"text\": \"Hej\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Feedback type is required"));
        mvc.perform(postAsStudent("{\"type\": \"APP\", \"text\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Feedback text is required"));
        verifyNoInteractions(service);
    }

    @Test
    void textOverTheMaxLengthGives400() throws Exception {
        String tooLong = "a".repeat(2001);
        mvc.perform(postAsStudent("{\"type\": \"APP\", \"text\": \"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Feedback text can be at most 2000 characters"));
        verifyNoInteractions(service);
    }

    @Test
    void dailyLimitGives429() throws Exception {
        doThrow(new TooManyRequestsException("Too much feedback sent today. Please try again tomorrow."))
                .when(service).send(eq(EMAIL), any());

        mvc.perform(postAsStudent("{\"type\": \"APP\", \"text\": \"Hej\"}"))
                .andExpect(status().isTooManyRequests());
    }
}
