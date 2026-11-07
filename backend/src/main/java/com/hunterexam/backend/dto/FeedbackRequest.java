package com.hunterexam.backend.dto;

import com.hunterexam.backend.entity.Feedback;
import com.hunterexam.backend.entity.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for sending feedback: {@code POST /api/feedback}.
 * <p>
 * {@code questionId} is only sent when a student reports a specific question.
 */
@Getter
@Setter
@NoArgsConstructor
public class FeedbackRequest {

    @NotNull(message = "Feedback type is required")
    private FeedbackType type;

    @NotBlank(message = "Feedback text is required")
    @Size(max = Feedback.MAX_TEXT_LENGTH,
            message = "Feedback text can be at most " + Feedback.MAX_TEXT_LENGTH + " characters")
    private String text;

    private Long questionId;
}
