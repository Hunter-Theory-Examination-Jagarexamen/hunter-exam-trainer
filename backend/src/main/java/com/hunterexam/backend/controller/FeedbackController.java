package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.FeedbackRequest;
import com.hunterexam.backend.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Lets any logged-in user send feedback or report a problem with a question. */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * Saves the feedback. Answers 201 with no body; the sender is never returned.
     *
     * @param authentication authentication info from Spring Security
     * @param request        type, text and optional question id
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void send(Authentication authentication, @Valid @RequestBody FeedbackRequest request) {
        feedbackService.send(authentication.getName(), request);
    }
}
