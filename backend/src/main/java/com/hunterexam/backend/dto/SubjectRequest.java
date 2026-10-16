package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Data transfer object used for creating and updating subject categories.
@Getter
@Setter
@NoArgsConstructor
public class SubjectRequest {

    // Ensures subject name is not null or bland
@NotBlank(message = "Name is required")
    private String name;

    // Ensures subject description is not null or blank
@NotBlank(message = "Description is required")
    private String description;

    // Custom constructor for convenient initialization
public SubjectRequest(String name, String description) {
    this.name = name;
    this.description = description;
}
}
