package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.DeleteAccountRequest;
import com.hunterexam.backend.dto.LearnerSummaryResponse;
import com.hunterexam.backend.dto.UpdateUserRoleRequest;
import com.hunterexam.backend.service.AccountDeletionService;
import com.hunterexam.backend.service.UserRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin user management. The {@code /api/admin/**} rule in SecurityConfig
 * already restricts this to admins; no extra annotation needed.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AccountDeletionService accountDeletionService;
    private final UserRoleService userRoleService;

    public AdminUserController(AccountDeletionService accountDeletionService,
                               UserRoleService userRoleService) {
        this.accountDeletionService = accountDeletionService;
        this.userRoleService = userRoleService;
    }

    /**
     * Deletes a student account. Admin accounts cannot be deleted here
     * (403 {@code AccessDeniedException}); they must delete themselves.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable Long id,
                              Authentication authentication,
                              @Valid @RequestBody DeleteAccountRequest request) {
        accountDeletionService.deleteStudentAsAdmin(id, authentication.getName(), request.password());
    }

    /**
     * Promotes a student to administrator or demotes an administrator to
     * student. Unknown id answers 404, a change that is not allowed (own
     * account, last administrator, role already the requested one) 409.
     * <p>
     * The acting administrator comes from the JWT, so the request body can
     * only ever name the target and the wanted role.
     *
     * @param id             id of the user to change
     * @param authentication the logged-in administrator
     * @param request        the wanted role ({@code ADMIN} or {@code STUDENT})
     * @return the user's name and new role
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<LearnerSummaryResponse> updateUserRole(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(
                userRoleService.changeRole(id, request.getRole(), authentication.getName()));
    }
}
