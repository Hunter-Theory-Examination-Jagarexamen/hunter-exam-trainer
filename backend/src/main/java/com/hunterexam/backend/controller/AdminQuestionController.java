package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.QuestionRequest;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** Question management, protected by the admin path security rule. */
@RestController
@RequestMapping("/api/admin/questions")
public class AdminQuestionController {
    private final QuestionService questionService;

    public AdminQuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Returns all questions in a subject, including the correct answer,
     * for the admin Question Bank. Students get questions without the
     * correct answer from the practice and mock exam endpoints.
     */
    @GetMapping
    public List<Question> getBySubject(@RequestParam Long subjectId) {
        return questionService.findBySubjectId(subjectId);
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
