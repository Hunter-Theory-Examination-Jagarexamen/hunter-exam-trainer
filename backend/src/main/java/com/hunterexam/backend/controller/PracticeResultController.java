package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.PracticeResultRequest;
import com.hunterexam.backend.service.PracticeResultService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/practice")
public class PracticeResultController {

    private final PracticeResultService practiceResultService;

    public PracticeResultController(PracticeResultService practiceResultService) {
        this.practiceResultService = practiceResultService;
    }

    @PostMapping("/results")
    public void savePracticeResult(
            Authentication authentication,
            @RequestBody PracticeResultRequest request) {

        String email = authentication.getName();

        practiceResultService.savePracticeResult(request, email);
    }
}
