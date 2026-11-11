package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.SubjectResponse;
import com.hunterexam.backend.service.SubjectService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that the subject write endpoints under {@code /api/admin/subjects}
 * accept ADMIN and reject everyone else.
 */
@WebMvcTest(controllers = AdminSubjectController.class)
class AdminSubjectControllerTests extends ControllerTestBase {

    @MockitoBean SubjectService service;

    private static final String BODY = """
            {"name":"Test Subject","description":"Test description"}
            """;

    @Test
    void studentsCannotManageSubjects() throws Exception {
        var student = jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"));

        for (var request : List.of(
                post("/api/admin/subjects"),
                put("/api/admin/subjects/1"),
                delete("/api/admin/subjects/1"))) {
            mvc.perform(request.with(student)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(BODY))
                    .andExpect(status().isForbidden());
        }

        verifyNoInteractions(service);
    }

    @Test
    void anonymousCannotManageSubjects() throws Exception {
        for (var request : List.of(
                post("/api/admin/subjects"),
                put("/api/admin/subjects/1"),
                delete("/api/admin/subjects/1"))) {
            mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isUnauthorized());
        }

        verifyNoInteractions(service);
    }

    @Test
    void adminCanCreateUpdateAndDeleteSubjects() throws Exception {
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(service.createSubject(any())).thenReturn(new SubjectResponse(7L, "Test Subject", "Test description", 0));
        when(service.updateSubject(eq(7L), any())).thenReturn(new SubjectResponse(7L, "Renamed", "New description", 0));

        mvc.perform(post("/api/admin/subjects").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Test Subject"));

        mvc.perform(put("/api/admin/subjects/7").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\",\"description\":\"New description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"));

        mvc.perform(delete("/api/admin/subjects/7").with(admin))
                .andExpect(status().isNoContent());

        verify(service).createSubject(any());
        verify(service).updateSubject(eq(7L), any());
        verify(service).deleteSubject(7L);
    }

    @Test
    void tooLongNameIsRejectedBeforeCallingService() throws Exception {
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        String body = "{\"name\":\"" + "a".repeat(256) + "\",\"description\":\"Test description\"}";

        mvc.perform(post("/api/admin/subjects").with(admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name must be at most 255 characters"));

        verifyNoInteractions(service);
    }
}
