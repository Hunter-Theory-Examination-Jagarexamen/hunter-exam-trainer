package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.dto.QuestionRequest;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Handles question retrieval and administrative question management.
 * <p>
 * This service fetches questions belonging to a specific subject,
 * used for the "Practice by Subject" feature.
 */
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;

    public QuestionService(QuestionRepository questionRepository, SubjectRepository subjectRepository) {
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public Question create(QuestionRequest request) {
        Question question = new Question();
        applyRequest(question, request);
        return questionRepository.save(question);
    }

    @Transactional
    public Question update(Long id, QuestionRequest request) {
        Question question = findQuestion(id);
        applyRequest(question, request);
        return questionRepository.save(question);
    }

    @Transactional
    public void delete(Long id) {
        questionRepository.delete(findQuestion(id));
    }

    private Question findQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
    }

    private void applyRequest(Question question, QuestionRequest request) {
        if (!List.of(request.optionA(), request.optionB(), request.optionC(), request.optionD())
                .contains(request.correctAnswer())) {
            throw new IllegalArgumentException("Correct answer must match one of the option texts");
        }
        var subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        question.setQuestionText(request.questionText());
        question.setOptionA(request.optionA());
        question.setOptionB(request.optionB());
        question.setOptionC(request.optionC());
        question.setOptionD(request.optionD());
        question.setCorrectAnswer(request.correctAnswer());
        question.setExplanation(request.explanation());
        question.setSubject(subject);
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
