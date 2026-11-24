package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.LearnerSummaryResponse;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.service.AccountDeletionService;
import com.hunterexam.backend.service.UserRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
class AdminUserControllerTests extends ControllerTestBase {

    @MockitoBean AccountDeletionService accountDeletionService;
    @MockitoBean UserRoleService userRoleService;

    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority STUDENT = new SimpleGrantedAuthority("ROLE_STUDENT");

    private static final String PROMOTE = """
            {"role":"ADMIN"}
            """;
    private static final String DEMOTE = """
            {"role":"STUDENT"}
            """;

    @Test
    void anonymousAndStudentsCannotChangeRoles() throws Exception {
        mvc.perform(put("/api/admin/users/7/role")
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTE))
                .andExpect(status().isUnauthorized());

        mvc.perform(put("/api/admin/users/7/role")
                        .with(jwt().authorities(STUDENT))
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTE))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userRoleService);
    }

    @Test
    void adminPromotesAStudentAndReceivesTheNewRole() throws Exception {
        when(userRoleService.changeRole(eq(7L), eq(Role.ADMIN), any()))
                .thenReturn(new LearnerSummaryResponse(7L, "Anna Andersson", Role.ADMIN));

        mvc.perform(put("/api/admin/users/7/role")
                        .with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.fullName").value("Anna Andersson"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userRoleService).changeRole(eq(7L), eq(Role.ADMIN), any());
    }

    @Test
    void adminDemotesAnAdministratorAndReceivesTheNewRole() throws Exception {
        when(userRoleService.changeRole(eq(5L), eq(Role.STUDENT), any()))
                .thenReturn(new LearnerSummaryResponse(5L, "Zara Admin", Role.STUDENT));

        mvc.perform(put("/api/admin/users/5/role")
                        .with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(DEMOTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        verify(userRoleService).changeRole(eq(5L), eq(Role.STUDENT), any());
    }

    @Test
    void rejectedChangesKeepTheirStatusCodes() throws Exception {
        var admin = jwt().authorities(ADMIN);

        when(userRoleService.changeRole(eq(1L), eq(Role.STUDENT), any()))
                .thenThrow(new ConflictException("You cannot change your own role."));
        mvc.perform(put("/api/admin/users/1/role").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(DEMOTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You cannot change your own role."));

        when(userRoleService.changeRole(eq(99L), eq(Role.ADMIN), any()))
                .thenThrow(new NotFoundException("User not found"));
        mvc.perform(put("/api/admin/users/99/role").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(PROMOTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void missingOrUnknownRoleIsRejectedBeforeTheServiceRuns() throws Exception {
        var admin = jwt().authorities(ADMIN);

        mvc.perform(put("/api/admin/users/7/role").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/admin/users/7/role").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"SUPERUSER\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userRoleService);
    }

    @Test
    void deletingAStudentStillWorks() throws Exception {
        mvc.perform(delete("/api/admin/users/7").with(jwt().jwt(token -> token.subject("admin@example.test")).authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"secret123\"}"))
                .andExpect(status().isNoContent());
        verify(accountDeletionService).deleteStudentAsAdmin(7L, "admin@example.test", "secret123");
    }

    @Test
    void deletingAStudentRequiresANonBlankPasswordAndAdminAuthentication() throws Exception {
        mvc.perform(delete("/api/admin/users/7").with(jwt().authorities(ADMIN)))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/admin/users/7").with(jwt().authorities(ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\" \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/admin/users/7").with(jwt().authorities(STUDENT))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"secret123\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(accountDeletionService);
    }
}
