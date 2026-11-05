package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.dto.LearnerSummaryResponse;
import com.hunterexam.backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin endpoints for viewing learner progress. */
@RestController
@RequestMapping("/api/admin/learners")
public class AdminLearnerController {

    private final AdminService adminService;

    public AdminLearnerController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Returns every user (admins first, then students) as a light list of names.
     * Protected by the /api/admin/** ADMIN rule in SecurityConfig.
     */
    @GetMapping
    public ResponseEntity<List<LearnerSummaryResponse>> getLearners() {
        return ResponseEntity.ok(adminService.getLearners());
    }

    /**
     * Returns progress for one user (student or admin).
     * Unknown id gives 404 through GlobalExceptionHandler.
     *
     * @param userId the user's id from the URL
     */
    @GetMapping("/{userId}")
    public ResponseEntity<LearnerProgressResponse> getOneLearnerProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getLearnerProgress(userId));
    }
}
