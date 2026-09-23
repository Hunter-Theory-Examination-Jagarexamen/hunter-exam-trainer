package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles retrieval of questions belongs to the subject.
 * <p>
 * This service fetches questions belonging to a specific subject,
 * used for the "Practice by Subject" feature.
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }


    /**
     * Fetches all questions belonging to a given subject.
     *
     * @param subjectId id of the subject to fetch questions for
     * @return list of questions for the specified subject
     */
    public List<Question> findBySubjectId(Long subjectId) {

        // Subject id must be provided to know which questions to return.
        if(subjectId == null) {
            throw new RuntimeException("Subject id is required.");
        }

        return questionRepository.findBySubjectId(subjectId);
    }
}
