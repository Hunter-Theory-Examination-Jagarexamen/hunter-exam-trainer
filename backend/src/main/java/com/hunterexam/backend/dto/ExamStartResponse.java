package com.hunterexam.backend.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ExamStartResponse {

    private Long attemptId;

    private List<QuestionResponse> questions;

    public ExamStartResponse(Long attemptId, List<QuestionResponse> questions) {
        this.attemptId = attemptId;
        this.questions = questions;
    }

}
