package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * REST controller for user statistics.
 * <p>
 * Provides endpoints for retrieving the authenticated user's
 * learning and practice performance.
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }


    /**
     * Returns the authenticated user's performance for practiced subjects.
     * <p>
     * The user's email is obtained from the JWT authentication context
     * so that statistics are retrieved only for the currently logged-in user.
     *
     * @param authentication authentication information from Spring Security
     * @return subject performance data
     */
    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectPerformanceResponse>> getSubjectPerformance(
            Authentication authentication) {

        String email = authentication.getName();

        List<SubjectPerformanceResponse> responses =
                statisticsService.getSubjectPerformance(email);

        return ResponseEntity.ok(responses);
    }

    /**
     * Returns the authenticated user's practice accuracy grouped by week,
     * for the last {@code weeks} weeks (default 8).
     *
     * @param authentication authentication info from Spring Security
     * @param weeks          number of weeks to look back (1–52)
     * @return weekly progress data, oldest first
     */
    @GetMapping("/progress")
    public ResponseEntity<List<ProgressOverTimeResponse>> getProgressOverTime(
            Authentication authentication,
            @RequestParam(defaultValue = "8") int weeks) {

        // Guard against invalid values
        int safeWeeks = Math.max(1, Math.min(weeks, 52));

        return ResponseEntity.ok(
                statisticsService.getProgressOverTime(
                        authentication.getName(),
                        safeWeeks
                )
        );
    }
}
