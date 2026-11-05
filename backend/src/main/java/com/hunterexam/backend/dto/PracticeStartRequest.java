package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for starting a practice round with one subject:
 * {@code POST /api/practice/attempts}.
 * <p>
 * A random round (#151) has no body: it is started with
 * {@code POST /api/practice/attempts/random}, where the server picks the
 * questions from the whole bank.
 */
@Getter
@Setter
@NoArgsConstructor
public class PracticeStartRequest {

    @NotNull(message = "Subject ID is required")
    private Long subjectId;
}
