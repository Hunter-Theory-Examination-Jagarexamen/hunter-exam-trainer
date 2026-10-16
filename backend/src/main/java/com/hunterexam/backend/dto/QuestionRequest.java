package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Input for creating or fully replacing a question. correctAnswer is option text. */
public record QuestionRequest(
        @NotBlank @Size(max = 255) String questionText,
        @NotBlank @Size(max = 255) String optionA,
        @NotBlank @Size(max = 255) String optionB,
        @NotBlank @Size(max = 255) String optionC,
        @NotBlank @Size(max = 255) String optionD,
        @NotBlank @Size(max = 255) String correctAnswer,
        @Size(max = 255) String explanation,
        @Size(max = 1000, message = "Image URL must not exceed 1000 characters") String imageUrl,
        @NotNull @Positive Long subjectId
) {
}
