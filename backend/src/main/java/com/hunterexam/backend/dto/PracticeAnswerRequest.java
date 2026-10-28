package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for answering one question in a practice round:
 * {@code POST /api/practice/attempts/{id}/answers}.
 */
@Getter
@Setter
@NoArgsConstructor
public class PracticeAnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Chosen option is required")
    @Pattern(regexp = "[ABCD]", message = "Chosen option must be A, B, C or D")
    private String chosenOption;
}
