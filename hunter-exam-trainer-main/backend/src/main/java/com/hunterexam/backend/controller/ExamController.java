package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.ExamQuestionResponse;
import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.service.ExamService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * @return list of questions for the mock exam
     */
    @PostMapping("/start") 
    public List<ExamQuestionResponse> startExam() {

        return examService.startExam();
    }


    /**
     * Submits a completed mock exam for evaluation.
     * <p>
     * The user's email is obtained from the JWT authentication context.
     * The service evaluates the submitted answers and stores the exam result.
     *
     * @param authentication authentication information from Spring Security
     * @param request submitted question IDs and user answers
     * @return calculated exam result
     */
    @PostMapping("/submit")
    public ExamResultResponse submitExam(
            Authentication authentication,
            @RequestBody ExamSubmitRequest request
    ) {

        String email = authentication.getName();

        return examService.submitExam(request, email);
    }
}
