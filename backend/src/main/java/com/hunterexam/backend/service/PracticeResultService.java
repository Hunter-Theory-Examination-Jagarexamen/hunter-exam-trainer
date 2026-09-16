package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.PracticeResultRequest;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.PracticeResultRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PracticeResultService {

    private final PracticeResultRepository practiceResultRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public PracticeResultService(
            PracticeResultRepository practiceResultRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository
    ) {
        this.practiceResultRepository = practiceResultRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    public void savePracticeResult(PracticeResultRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (request.getTotalQuestions() <= 0) {
            throw new RuntimeException("Total Questions must be greater than zero");
        }

        long score = Math.round(
                (double) request.getCorrectAnswers()
                        / request.getTotalQuestions() * 100);

        PracticeResult practiceResult = new PracticeResult();

        practiceResult.setCorrectAnswers(request.getCorrectAnswers());
        practiceResult.setTotalQuestions(request.getTotalQuestions());
        practiceResult.setScore(score);
        practiceResult.setCompletedAt(LocalDateTime.now());
        practiceResult.setUser(user);
        practiceResult.setSubject(subject);

        practiceResultRepository.save(practiceResult);
    }
}
