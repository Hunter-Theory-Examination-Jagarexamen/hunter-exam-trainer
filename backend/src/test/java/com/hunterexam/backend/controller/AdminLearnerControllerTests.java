package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.dto.LearnerSummaryResponse;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminLearnerController.class)
class AdminLearnerControllerTests extends ControllerTestBase {

    @MockitoBean AdminService service;

    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    @Test
    void anonymousAndStudentsCannotReadTheList() throws Exception {
        mvc.perform(get("/api/admin/learners"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/learners")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }

    @Test
    void adminReceivesTheListWithRoles() throws Exception {
        when(service.getLearners()).thenReturn(List.of(
                new LearnerSummaryResponse(5L, "Zara Admin", Role.ADMIN),
                new LearnerSummaryResponse(7L, "Anna Andersson", Role.STUDENT)));

        mvc.perform(get("/api/admin/learners").with(jwt().authorities(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("Zara Admin"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].role").value("STUDENT"))
                .andExpect(jsonPath("$[1].email").doesNotExist());
    }

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
                7L, "Anna Andersson", "anna@example.com", null, null, null, List.of(), null));

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
