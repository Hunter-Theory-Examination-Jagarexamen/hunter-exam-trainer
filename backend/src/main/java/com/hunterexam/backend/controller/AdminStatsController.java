package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.AdminStatsResponse;
import com.hunterexam.backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Overview counts for the admin start page, protected by the admin path security rule. */
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final AdminService adminService;

    public AdminStatsController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Returns the question, subject and student counts for the overview.
     * Protected by the /api/admin/** ADMIN rule in SecurityConfig, so an
     * authenticated student receives 403 and an anonymous request 401.
     *
     * @return the overview counts
     */
    @GetMapping
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
