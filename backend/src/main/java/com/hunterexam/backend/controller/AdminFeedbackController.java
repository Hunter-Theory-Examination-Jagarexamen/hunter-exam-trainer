package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.FeedbackHandledRequest;
import com.hunterexam.backend.dto.FeedbackResponse;
import com.hunterexam.backend.entity.FeedbackType;
import com.hunterexam.backend.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin endpoints for reading and handling feedback.
 * Protected by the /api/admin/** ADMIN rule in SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/feedback")
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    public AdminFeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * Lists feedback, newest first. Both filters are optional.
     *
     * @param type    only this type (QUESTION, APP, OTHER)
     * @param handled only handled ({@code true}) or unhandled ({@code false}) items
     */
    @GetMapping
    public List<FeedbackResponse> list(
            @RequestParam(required = false) FeedbackType type,
            @RequestParam(required = false) Boolean handled
    ) {
        return feedbackService.listForAdmin(type, handled);
    }

    /** Marks one item as handled or unhandled; 404 if it doesn't exist. */
    @PatchMapping("/{id}/handled")
    public FeedbackResponse setHandled(@PathVariable Long id, @Valid @RequestBody FeedbackHandledRequest request) {
        return feedbackService.setHandled(id, request.getHandled());
    }
}
