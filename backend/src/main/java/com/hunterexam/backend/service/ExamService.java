package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamQuestionResponse;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ExamService {

    private final QuestionRepository questionRepository;

    public ExamService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<ExamQuestionResponse> startExam() {

        List<Question> questions = questionRepository.findAll();

        if (questions.size() < 70) {
            throw new RuntimeException(
                    "Not enough questions available to start the exam."
            );
        }

        Collections.shuffle(questions);

        return questions.stream()
                .limit(70)
                .map(question -> new ExamQuestionResponse(
                        question.getId(),
                        question.getQuestionText(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD()
                ))
                .toList();
    }
}
