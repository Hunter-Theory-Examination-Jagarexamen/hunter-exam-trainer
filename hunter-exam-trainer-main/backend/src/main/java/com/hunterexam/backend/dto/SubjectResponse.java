package com.hunterexam.backend.dto;

import lombok.Getter;

/**
 * Response containing information about a hunter exam subject.
 * <p>
 * Includes the subject ID, name, description, and number
 * of available questions.
 */
@Getter
public class SubjectResponse {

    private Long id;
    private String name;
    private String description;
    private long questionCount;

    public SubjectResponse(
            Long id,
            String name,
            String description,
            long questionCount
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.questionCount = questionCount;
    }
}
