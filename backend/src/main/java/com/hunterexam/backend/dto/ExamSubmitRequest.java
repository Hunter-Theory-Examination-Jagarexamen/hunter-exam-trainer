package com.hunterexam.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request data used when submitting the exam: only which exam.
 * The answers are already on the server (saved one by one with
 * {@code POST /api/exam/{attemptId}/answer}), so the client doesn't send them.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExamSubmitRequest {

    private Long attemptId;
}
