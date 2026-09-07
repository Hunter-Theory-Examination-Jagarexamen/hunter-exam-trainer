package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.SubjectResponse;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService  {

    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;

    public SubjectService(
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository)
    {
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
    }

    public List<SubjectResponse> findAllSubjects() {

        return subjectRepository.findAll()
                .stream()
                .map(subject -> new SubjectResponse(
                        subject.getId(),
                        subject.getName(),
                        subject.getDescription(),
                        questionRepository.countBySubjectId(subject.getId())
                ))
                .toList();
    }
}
