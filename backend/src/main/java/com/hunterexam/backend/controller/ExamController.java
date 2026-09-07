package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.ExamQuestionResponse;
import com.hunterexam.backend.service.ExamService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/start") 
    public List<ExamQuestionResponse> startExam() {

        return examService.startExam();
    }
}
