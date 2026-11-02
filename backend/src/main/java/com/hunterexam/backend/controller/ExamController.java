package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamStartResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.service.ExamService;
import com.hunterexam.backend.dto.ExamAnswerRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the mock hunter exam.
 * <p>
 * Provides endpoints for starting a mock exam, saving answers as they are
 * given, and submitting the completed exam for evaluation.
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
     * Saves a single answer on the current exam session.
     * <p>
     * Called as the user answers each question, so a lost connection,
     * expired token, or closed tab does not lose the attempt. Protected
     * by the same ownership rules as submit.
     *
     * @param authentication authentication info from Spring Security
     * @param sessionId      exam session id
     * @param request        the question id and chosen answer
     * @return 204 No Content on success
     */
    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<Void> saveAnswer(
        Authentication authentication,
        @PathVariable Long sessionId,
        @Valid @RequestBody ExamAnswerRequest request
    ) {
        examService.saveAnswer(
            sessionId,
            request.getQuestionId(),
            request.getAnswer(),
            authentication.getName()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/submit")
    public ExamResultResponse submitExam(
            Authentication authentication,
            @RequestBody ExamSubmitRequest request
    ) {
        return examService.submitExam(request, authentication.getName());
    }
}
