package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminLearnerController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class AdminLearnerControllerTests {

    @Autowired MockMvc mvc;
    @MockitoBean AdminService service;
    @MockitoBean OAuth2LoginSuccessHandler loginHandler;

    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    @Test
    void anonymousAndStudentsCannotReadOneLearner() throws Exception {
        mvc.perform(get("/api/admin/learners/7"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/learners/7")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void adminReceivesOneLearnersProgress() throws Exception {
        when(service.getLearnerProgress(7L)).thenReturn(new LearnerProgressResponse(
                7L, "Anna Andersson", "anna@example.com", null, null, List.of(), null));

        mvc.perform(get("/api/admin/learners/7").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.fullName").value("Anna Andersson"))
                .andExpect(jsonPath("$.email").value("anna@example.com"));
    }

    @Test
    void unknownLearnerGives404() throws Exception {
        when(service.getLearnerProgress(99L)).thenThrow(new NotFoundException("User not found"));

        mvc.perform(get("/api/admin/learners/99").with(jwt().authorities(ADMIN)))
                .andExpect(status().isNotFound());
    }
}
