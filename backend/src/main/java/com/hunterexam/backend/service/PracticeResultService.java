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

/**
 * Handles saving of practice session results.
 * <p>
 * This service records the outcome of a completed practice session
 * (correct/total answers for a subject), calculates the score, and
 * persists it for the logged-in user.
 */
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


    /**
     * Saves the result of a completed practice session.
     * <p>
     * Looks up the user and subject, validates the submitted question count,
     * calculates the score as a percentage, and stores the result.
     *
     * @param request practice result containing subject id, total questions and correct answers
     * @param email   user's email id
     */
    public void savePracticeResult(PracticeResultRequest request, String email) {

        // Find the logged-in user to associate the result with.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the subject this practice session belongs to.
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // Guard against division by zero when calculating the score below.
        if (request.getTotalQuestions() <= 0) {
            throw new RuntimeException("Total Questions must be greater than zero");
        }

        // Calculate the score as a percentage of correct answers.
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
