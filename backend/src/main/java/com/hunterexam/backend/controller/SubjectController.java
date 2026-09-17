package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.SubjectResponse;
import com.hunterexam.backend.service.SubjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
