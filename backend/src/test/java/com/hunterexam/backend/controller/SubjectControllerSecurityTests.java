package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.dto.SubjectResponse;
import com.hunterexam.backend.service.SubjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that reads on {@code /api/subjects} stay available to any
 * authenticated user, and that the old write paths are no longer
 * routed after the move to {@code /api/admin/subjects}.
 */
@WebMvcTest(controllers = SubjectController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class SubjectControllerSecurityTests {

    @Autowired MockMvc mvc;
    @MockitoBean SubjectService service;
    @MockitoBean OAuth2LoginSuccessHandler loginHandler;

    private static final String BODY = """
            {"name":"Test Subject","description":"Test description"}
            """;

    @Test
    void studentCanReadSubjects() throws Exception {
        when(service.findAllSubjects()).thenReturn(List.of());
        when(service.findSubjectById(1L)).thenReturn(new SubjectResponse(1L, "Test", "Desc", 0));

        var student = jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"));

        mvc.perform(get("/api/subjects").with(student))
                .andExpect(status().isOk());

        mvc.perform(get("/api/subjects/1").with(student))
                .andExpect(status().isOk());
    }

    @Test
    void oldWriteRoutesAreNotWritableByStudents() throws Exception {
        var student = jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"));

        for (var request : List.of(
                post("/api/subjects"),
                put("/api/subjects/1"),
                delete("/api/subjects/1"))) {
            mvc.perform(request.with(student)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BODY))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(service);
    }

    @Test
    void anonymousCannotUseOldWriteRoutes() throws Exception {
        for (var request : List.of(
                post("/api/subjects"),
                put("/api/subjects/1"),
                delete("/api/subjects/1"))) {
            mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isUnauthorized());
        }

        verifyNoInteractions(service);
    }

    @Test
    void oldWriteRoutesAreNoLongerMappedEvenForAdmins() throws Exception {
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // The routes are gone: no handler method answers them (405), so they
        // cannot be used as aliases for /api/admin/subjects.
        for (var request : List.of(
                post("/api/subjects"),
                put("/api/subjects/1"),
                delete("/api/subjects/1"))) {
            mvc.perform(request.with(admin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BODY))
                    .andExpect(status().isMethodNotAllowed());
        }

        verifyNoInteractions(service);
    }
}
