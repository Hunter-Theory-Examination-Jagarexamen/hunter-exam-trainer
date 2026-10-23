package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamStartResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.service.ExamService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the mock hunter exam.
 * <p>
 * Provides endpoints for starting a mock exam and submitting
 * the completed exam for evaluation.
 */
@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    /**
     * Starts a new mock exam.
     * <p>
     * The service selects 70 questions and returns them without
     * exposing the correct answers to the client.
     *
     * @param authentication authentication info from Spring Security
     * @return session id and the 70 questions for this attempt
     */
    @PostMapping("/start")
    public ExamStartResponse startExam(Authentication authentication) {
        return examService.startExam(authentication.getName());
    }

    /**
     * Submits a completed mock exam for evaluation.
     * <p>
     * The user's email is obtained from the JWT authentication context.
     * The service evaluates the submitted answers and stores the exam result.
     *
     * @param authentication authentication information from Spring Security
     * @param request submitted answers (the client's question list is ignored)
     * @return calculated exam result
     */
    @PostMapping("/submit")
    public ExamResultResponse submitExam(
            Authentication authentication,
            @RequestBody ExamSubmitRequest request
    ) {
        return examService.submitExam(request, authentication.getName());
    }
}