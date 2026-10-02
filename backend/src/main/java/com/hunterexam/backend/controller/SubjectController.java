package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.SubjectRequest;
import com.hunterexam.backend.dto.SubjectResponse;
import com.hunterexam.backend.service.SubjectService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for hunter exam subjects.
 * <p>
 * Provides an endpoint for retrieving all available subjects
 * together with their question counts.
 */
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }


    /**
     * Returns all available hunter exam subjects.
     * <p>
     * The service also provides the number of questions available
     * for each subject.
     *
     * @return list of subject information
     */
    @GetMapping
    public List<SubjectResponse> getAllSubjects() {

        return subjectService.findAllSubjects();
    }

    // Returns a specific subject by ID
    @GetMapping("/{id}")
    public SubjectResponse getSubjectById(@PathVariable Long id) {
        return subjectService.findSubjectById(id);
    }

    // Creates new subject category (Restricted to ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponse createSubject(@Valid @RequestBody SubjectRequest request) {
        return subjectService.createSubject(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectResponse updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return subjectService.updateSubject(id, request);
    }

    // Deletes a subject category by ID (Restricted to ADMIN)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
    }

}
