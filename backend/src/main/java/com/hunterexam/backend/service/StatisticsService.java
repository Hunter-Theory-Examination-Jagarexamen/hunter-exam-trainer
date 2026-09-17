package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.PracticeResultRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles calculation of practice performance statistics.
 * <p>
 * This service aggregates a user's practice results by subject and
 * calculates the average score per subject, used for the Statistics page.
 */
@Service
public class StatisticsService {

    private final PracticeResultRepository practiceResultRepository;
    private final UserRepository userRepository;

    public StatisticsService(
            PracticeResultRepository practiceResultRepository,
            UserRepository userRepository
    ) {
        this.practiceResultRepository = practiceResultRepository;
        this.userRepository = userRepository;
    }


    /**
     * Calculates the user's average practice performance per subject.
     * <p>
     * Groups all of the user's practice results by subject and computes
     * the average score for each. Subjects the user has not practiced
     * are not included in the result.
     *
     * @param email user's email id
     * @return list of subject performance responses (subject id, name and average score)
     */
    public List<SubjectPerformanceResponse> getSubjectPerformance(String email) {

        // Find the logged-in user to fetch their practice results.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PracticeResult> results = practiceResultRepository.findByUser(user);

        // Group all practice results by subject id, so we can average scores per subject.
        Map<Long, List<PracticeResult>> resultsBySubject = results.stream()
                .collect(Collectors.groupingBy(
                        result -> result.getSubject().getId()));

        return resultsBySubject.values()
                .stream()
                .map(subjectResults -> {

                    // Used only to read the subject id/name, since all results in this
                    // group belong to the same subject.
                    PracticeResult firstResult = subjectResults.getFirst();

                    // Average score across all practice attempts for this subject.
                    long averagePercentage = Math.round(
                            subjectResults.stream()
                                    .mapToLong(PracticeResult::getScore)
                                    .average()
                                    .orElse(0)
                    );

                    return new SubjectPerformanceResponse(
                            firstResult.getSubject().getId(),
                            firstResult.getSubject().getName(),
                            averagePercentage
                    );
                })
                .toList();
    }
}
