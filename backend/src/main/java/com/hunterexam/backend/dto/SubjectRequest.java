package com.hunterexam.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    // Ensures subject description is not null or blank
@NotBlank(message = "Description is required")
@Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    // Custom constructor for convenient initialization
public SubjectRequest(String name, String description) {
    this.name = name;
    this.description = description;
}
}
