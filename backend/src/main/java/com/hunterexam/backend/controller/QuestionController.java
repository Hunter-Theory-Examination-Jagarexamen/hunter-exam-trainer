package com.hunterexam.backend.controller;

import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for retrieving practice questions.
 * <p>
 * Provides endpoints for retrieving questions based on their subject.
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }


    /**
     * Returns all questions belonging to a specific subject.
     *
     * @param subjectId ID of the subject
     * @return list of questions for the selected subject
     */
    @GetMapping
    public List<Question> getQuestionsBySubject(@RequestParam Long subjectId) {

        return questionService.findBySubjectId(subjectId);
    }
}
