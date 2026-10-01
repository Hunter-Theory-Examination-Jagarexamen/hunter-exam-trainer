package com.hunterexam.backend.dto;

import com.hunterexam.backend.entity.ExamSession;
import lombok.Getter;

import java.util.List;

@Getter
public class ExamStartResponse {

    private Long sessionId;

    private List<ExamQuestionResponse> questions;

    public ExamStartResponse(Long sessionId, List<ExamQuestionResponse> questions) {
        this.sessionId = sessionId;
        this.questions = questions;
    }

}
