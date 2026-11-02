package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for saving a single exam answer while the exam is in progress.
 *
 * <p>
 * Sent to {@code POST /api/exam/{sessionId}/answer} each time the user selects or changes an answer.
 * The server stores it on the session so that a lost connection, expired token, or closed tab does not lose the attempt.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExamAnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Answer is required")
    private String answer;
}
