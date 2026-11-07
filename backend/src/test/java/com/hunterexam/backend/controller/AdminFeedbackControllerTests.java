package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.dto.FeedbackResponse;
import com.hunterexam.backend.entity.FeedbackType;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminFeedbackController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class AdminFeedbackControllerTests {

    @Autowired MockMvc mvc;
    @MockitoBean FeedbackService service;
    @MockitoBean OAuth2LoginSuccessHandler loginHandler;

    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority STUDENT = new SimpleGrantedAuthority("ROLE_STUDENT");

    private final FeedbackResponse report = new FeedbackResponse(
            3L, FeedbackType.QUESTION, "Fel svar", LocalDateTime.now(), false, 12L, "Vilket djur?");

    @Test
    void anonymousAndStudentsCannotReadOrHandleFeedback() throws Exception {
        mvc.perform(get("/api/admin/feedback"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/feedback").with(jwt().authorities(STUDENT)))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/admin/feedback/3/handled").with(jwt().authorities(STUDENT))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"handled\": true}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void adminReceivesTheListWithoutASender() throws Exception {
        when(service.listForAdmin(null, null)).thenReturn(List.of(report));

        mvc.perform(get("/api/admin/feedback").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("QUESTION"))
                .andExpect(jsonPath("$[0].questionId").value(12))
                .andExpect(jsonPath("$[0].questionText").value("Vilket djur?"))
                .andExpect(jsonPath("$[0].sender").doesNotExist());
    }

    @Test
    void filtersArePassedToTheService() throws Exception {
        when(service.listForAdmin(FeedbackType.QUESTION, false)).thenReturn(List.of(report));

        mvc.perform(get("/api/admin/feedback?type=QUESTION&handled=false").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void unknownTypeGives400() throws Exception {
        mvc.perform(get("/api/admin/feedback?type=NONSENSE").with(jwt().authorities(ADMIN)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for 'type'"));
    }

    @Test
    void adminMarksAnItemAsHandled() throws Exception {
        when(service.setHandled(3L, true)).thenReturn(new FeedbackResponse(
                3L, FeedbackType.QUESTION, "Fel svar", LocalDateTime.now(), true, 12L, "Vilket djur?"));

        mvc.perform(patch("/api/admin/feedback/3/handled").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"handled\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handled").value(true));
    }

    @Test
    void handlingAnUnknownItemGives404AndAMissingValueGives400() throws Exception {
        when(service.setHandled(99L, true)).thenThrow(new NotFoundException("Feedback not found"));

        mvc.perform(patch("/api/admin/feedback/99/handled").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"handled\": true}"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/admin/feedback/3/handled").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void browsersMayCallPatchFromTheFrontend() throws Exception {
        // The browser asks first (a CORS "preflight") whether PATCH is allowed from the
        // frontend's address; without PATCH in the CORS config it blocks the request.
        mvc.perform(options("/api/admin/feedback/3/handled")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods",
                        containsString("PATCH")));
    }
}
