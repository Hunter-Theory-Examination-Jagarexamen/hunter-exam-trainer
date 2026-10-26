package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.dto.AdminStatsResponse;
import com.hunterexam.backend.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminStatsController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class AdminStatsControllerTests {

    @Autowired MockMvc mvc;
    @MockitoBean AdminService service;
    @MockitoBean OAuth2LoginSuccessHandler loginHandler;

    @Test
    void anonymousAndStudentsCannotReadStats() throws Exception {
        mvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/stats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void adminReceivesQuestionSubjectAndStudentCounts() throws Exception {
        when(service.getStats()).thenReturn(new AdminStatsResponse(42, 5, 17));

        mvc.perform(get("/api/admin/stats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionCount").value(42))
                .andExpect(jsonPath("$.subjectCount").value(5))
                .andExpect(jsonPath("$.studentCount").value(17));
    }
}
