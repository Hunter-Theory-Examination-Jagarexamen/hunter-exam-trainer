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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that subject write endpoints are admin-only, while
 * reads remain available to any authenticated user.
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
    void studentCannotWriteSubjects() throws Exception {
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
    void anonymousCannotWriteSubjects() throws Exception {
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
    void adminCanWriteSubjects() throws Exception {
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(service.createSubject(any())).thenReturn(new SubjectResponse(1L, "Test", "Desc", 0));
        when(service.updateSubject(any(), any())).thenReturn(new SubjectResponse(1L, "Test", "Desc", 0));

        mvc.perform(post("/api/subjects").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated());

        mvc.perform(put("/api/subjects/1").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/subjects/1").with(admin))
                .andExpect(status().isNoContent());
    }
}