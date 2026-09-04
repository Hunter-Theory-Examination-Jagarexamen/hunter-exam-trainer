package com.hunterexam.backend.controller;

import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<Question> getQuestionsBySubject(@Valid @RequestParam Long subjectId) {

        return questionService.findBySubjectId(subjectId);
    }
}
