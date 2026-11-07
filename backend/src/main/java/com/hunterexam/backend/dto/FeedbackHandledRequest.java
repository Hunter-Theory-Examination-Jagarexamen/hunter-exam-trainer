package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for {@code PATCH /api/admin/feedback/{id}/handled}. */
@Getter
@Setter
@NoArgsConstructor
public class FeedbackHandledRequest {

    @NotNull(message = "Handled is required")
    private Boolean handled;
}
