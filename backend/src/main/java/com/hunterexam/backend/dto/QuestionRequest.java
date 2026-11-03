package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/** Input for creating or fully replacing a question. correctAnswer is the letter A, B, C, or D. */
public record QuestionRequest(
        @NotBlank @Size(max = 255) String questionText,
        @NotBlank @Size(max = 255) String optionA,
        @NotBlank @Size(max = 255) String optionB,
        @NotBlank @Size(max = 255) String optionC,
        @NotBlank @Size(max = 255) String optionD,
        @NotBlank @Pattern(regexp = "^[A-D]$", message = "Correct answer must be A, B, C, or D") String correctAnswer,
        @Size(max = 255) String explanation,
        @Size(max = 1000, message = "Image URL must not exceed 1000 characters") String imageUrl,
        @NotNull @Positive Long subjectId
) {
}
