package com.hunterexam.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Request data used when submitting the exam.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExamSubmitRequest {

    private List<Long> questionIds;

    private Map<String, String> answers;
}
