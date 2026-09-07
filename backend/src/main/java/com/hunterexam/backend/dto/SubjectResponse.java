package com.hunterexam.backend.dto;

import lombok.Getter;

@Getter
public class SubjectResponse {

    private Long id;
    private String name;
    private String description;
    private long questionCount;

    public SubjectResponse(Long id, String name, String description, long questionCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.questionCount = questionCount;
    }
}
