package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.QuestionRequest;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/** Question management, protected by the admin path security rule. */
@RestController
@RequestMapping("/api/admin/questions")
public class AdminQuestionController {
    private final QuestionService questionService;

    public AdminQuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<Question> create(@Valid @RequestBody QuestionRequest request) {
        Question question = questionService.create(request);
        return ResponseEntity.created(URI.create("/api/admin/questions/" + question.getId()))
                .body(question);
    }

    @PutMapping("/{id}")
    public Question update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return questionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
