package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.PracticeResultRequest;
import com.hunterexam.backend.service.PracticeResultService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for practice session results.
 * <p>
 * Provides an endpoint for saving the result of a completed
 * subject-based practice session.
 */
@RestController
@RequestMapping("/api/practice")
public class PracticeResultController {

    private final PracticeResultService practiceResultService;

    public PracticeResultController(PracticeResultService practiceResultService) {
        this.practiceResultService = practiceResultService;
    }


    /**
     * Saves the result of a completed practice session.
     * <p>
     * The authenticated user's email is obtained from the JWT
     * authentication context. The service calculates the score
     * and stores the result for future statistics.
     *
     * @param authentication authentication information from Spring Security
     * @param request practice result details submitted by the client
     */
    @PostMapping("/results")
    public void savePracticeResult(
            Authentication authentication,
            @RequestBody PracticeResultRequest request) {

        String email = authentication.getName();

        practiceResultService.savePracticeResult(request, email);
    }
}
