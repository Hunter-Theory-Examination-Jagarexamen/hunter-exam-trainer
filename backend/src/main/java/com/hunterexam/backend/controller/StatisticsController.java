package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectPerformanceResponse>> getSubjectPerformance(
            Authentication authentication) {

        String email = authentication.getName();

        List<SubjectPerformanceResponse> responses =
                statisticsService.getSubjectPerformance(email);

        return ResponseEntity.ok(responses);
    }
}
