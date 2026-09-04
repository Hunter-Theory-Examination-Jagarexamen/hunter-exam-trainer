package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> findBySubjectId(Long subjectId) {

        if(subjectId == null) {
            throw new RuntimeException("Subject id is required.");
        }

        return questionRepository.findBySubjectId(subjectId);
    }
}
