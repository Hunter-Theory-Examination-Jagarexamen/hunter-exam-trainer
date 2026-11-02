package com.hunterexam.backend.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class ExamStartResponse {

    private Long sessionId;

    private List<QuestionResponse> questions;

    public ExamStartResponse(Long sessionId, List<QuestionResponse> questions) {
        this.sessionId = sessionId;
        this.questions = questions;
    }

}
