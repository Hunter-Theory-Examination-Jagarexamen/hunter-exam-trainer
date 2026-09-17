package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.SubjectResponse;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles retrieval of hunter-exam subjects.
 * <p>
 * This service fetches all available subjects along with the number of
 * questions in each, used for the subject selection screen.
 */
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


    /**
     * Fetches all subjects along with their question count.
     *
     * @return list of subject responses (id, name, description and question count)
     */
    public List<SubjectResponse> findAllSubjects() {

        return subjectRepository.findAll()
                .stream()
                .map(subject -> new SubjectResponse(
                        subject.getId(),
                        subject.getName(),
                        subject.getDescription(),
                        // Count questions for this subject separately, since Subject
                        // doesn't hold a direct reference to its questions.
                        questionRepository.countBySubjectId(subject.getId())
                ))
                .toList();
    }
}
