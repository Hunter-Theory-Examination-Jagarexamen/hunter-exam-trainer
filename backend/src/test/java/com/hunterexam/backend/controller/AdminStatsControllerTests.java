package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.AdminStatsResponse;
import com.hunterexam.backend.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminStatsController.class)
class AdminStatsControllerTests extends ControllerTestBase {

    @MockitoBean AdminService service;

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
        when(service.getStats()).thenReturn(new AdminStatsResponse(42, 5, 17, 3));

        mvc.perform(get("/api/admin/stats")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionCount").value(42))
                .andExpect(jsonPath("$.subjectCount").value(5))
                .andExpect(jsonPath("$.studentCount").value(17))
                .andExpect(jsonPath("$.unhandledFeedbackCount").value(3));
    }
}
