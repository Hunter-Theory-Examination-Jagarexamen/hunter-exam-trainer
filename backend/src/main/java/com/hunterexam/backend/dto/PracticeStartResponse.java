package com.hunterexam.backend.dto;

import lombok.Getter;

import java.util.List;

/**
 * Response when a practice round has started: the round's id and its
 * questions, without the correct answers (the server checks each answer).
 */
@Getter
public class PracticeStartResponse {

    private final Long attemptId;
    // For the page title; the questions don't carry their subject.
    // Null for a random round (#151), which mixes subjects.
    private final String subjectName;

    // Reuses the mock exam's question shape, which already leaves out the correct answer.
    private final List<QuestionResponse> questions;

    public PracticeStartResponse(Long attemptId, String subjectName, List<QuestionResponse> questions) {
        this.attemptId = attemptId;
        this.subjectName = subjectName;
        this.questions = questions;
    }
}
