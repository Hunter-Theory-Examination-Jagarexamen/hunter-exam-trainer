package com.hunterexam.backend.dto;

import com.hunterexam.backend.entity.Feedback;
import com.hunterexam.backend.entity.FeedbackType;

import java.time.LocalDateTime;

/**
 * One feedback item as admins see it. Has no sender field on purpose:
 * admins must not see who sent it.
 * <p>
 * For a question report, {@code questionId} is empty if the question has been
 * deleted; {@code questionText} is the text when it was reported.
 */
public record FeedbackResponse(
        Long id,
        FeedbackType type,
        String text,
        LocalDateTime createdAt,
        boolean handled,
        Long questionId,
        String questionText
) {
    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getType(),
                feedback.getText(),
                feedback.getCreatedAt(),
                feedback.isHandled(),
                feedback.getQuestion() == null ? null : feedback.getQuestion().getId(),
                feedback.getQuestionTextSnapshot());
    }
}
