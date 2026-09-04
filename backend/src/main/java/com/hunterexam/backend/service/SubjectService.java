package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService  {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> findAllSubjects() {

        return subjectRepository.findAll();
    }
}
