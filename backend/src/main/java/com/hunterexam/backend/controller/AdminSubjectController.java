package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.SubjectRequest;
import com.hunterexam.backend.dto.SubjectResponse;
import com.hunterexam.backend.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Subject write operations, protected by the admin path security rule
 * and by method-level security.
 * <p>
 * Creating, updating and deleting subjects lives here so every write
 * follows the {@code /api/admin/**} convention. Reads stay on
 * {@code /api/subjects} in {@link SubjectController}.
 */
@RestController
@RequestMapping("/api/admin/subjects")
public class AdminSubjectController {

    private final SubjectService subjectService;

    public AdminSubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    // Creates a new subject category (201)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponse createSubject(@Valid @RequestBody SubjectRequest request) {
        return subjectService.createSubject(request);
    }

    // Updates an existing subject category (200)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponse updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return subjectService.updateSubject(id, request);
    }

    // Deletes a subject category by ID (204)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
    }
}
