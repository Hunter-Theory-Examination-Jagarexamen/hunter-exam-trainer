package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.DashboardResponse;
import com.hunterexam.backend.dto.RecentActivityResponse;
import com.hunterexam.backend.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for dashboard data.
 * <p>
 * Provides endpoints for retrieving the authenticated user's
 * dashboard statistics and recent activity.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }


    /**
     * Returns dashboard statistics for the currently authenticated user.
     * <p>
     * The user's email is obtained from the JWT authentication context
     * and passed to the service to retrieve user-specific statistics.
     *
     * @param authentication authentication information from Spring Security
     * @return dashboard statistics for the authenticated user
     */
    @GetMapping
    public DashboardResponse getDashboard(Authentication authentication) {

        String email = authentication.getName();

        return dashboardService.getDashboard(email);
    }


    /**
     * Returns the user's five most recent mock exam results.
     *
     * @param authentication authentication information from Spring Security
     * @return list of recent mock exam activities
     */
    @GetMapping("/recent")
    public List<RecentActivityResponse> getRecentActivity(Authentication authentication) {

        String email = authentication.getName();

        return dashboardService.getRecentActivity(email);
    }
}
