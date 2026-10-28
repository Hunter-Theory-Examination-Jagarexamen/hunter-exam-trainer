package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for starting a practice round:
 * {@code POST /api/practice/attempts}.
 * <p>
 * For now a round always covers one subject. Random practice (#151) will
 * add other ways to choose the questions.
 */
@Getter
@Setter
@NoArgsConstructor
public class PracticeStartRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;
}
