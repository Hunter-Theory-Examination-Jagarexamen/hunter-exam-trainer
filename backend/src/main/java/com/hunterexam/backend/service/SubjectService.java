package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.SubjectRequest;
import com.hunterexam.backend.dto.SubjectResponse;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    // Retrieves a single subject by ID or throws a 404 if not found.
    public SubjectResponse findSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found with id: " + id));

        return mapToResponse(subject);
    }

    // Creates a new subject category
    public SubjectResponse createSubject(SubjectRequest request) {
        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setDescription(request.getDescription());

        // Save entity to database
        Subject savedSubject = subjectRepository.save(subject);
        return mapToResponse(savedSubject);
    }

    // Updates an existing subject category
    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found with id: " + id));

        //Update entity fields from request
        subject.setName(request.getName());
        subject.setDescription(request.getDescription());

        // Save updated entity to database
        Subject updatedSubject = subjectRepository.save(subject);
        return mapToResponse(updatedSubject);
    }

    // Deletes a subject category by ID
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found with id: " + id);
        }

        // Delete entity from database
        subjectRepository.deleteById(id);
    }

    // Helper method to map a Subject entity to SubjectResponse DTO
    private SubjectResponse mapToResponse(Subject subject) {
        long questionCount = questionRepository.countBySubjectId(subject.getId());

        // Return populated DTO
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                questionCount
        );
    }


}
