package com.hunterexam.backend.controller;

import com.hunterexam.backend.service.AccountDeletionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    public AdminUserController(AccountDeletionService accountDeletionService) {
        this.accountDeletionService = accountDeletionService;
    }

    /**
     * Deletes a student account. Admin accounts cannot be deleted here
     * (403 {@code AccessDeniedException}); they must delete themselves.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable Long id) {
        accountDeletionService.deleteStudentAsAdmin(id);
    }
}
