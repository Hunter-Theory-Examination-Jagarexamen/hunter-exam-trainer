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

    public List<SubjectPerformanceResponse> getSubjectPerformance(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PracticeResult> results = practiceResultRepository.findByUser(user);

        Map<Long, List<PracticeResult>> resultsBySubject = results.stream()
                .collect(Collectors.groupingBy(
                        result -> result.getSubject().getId()));

        return resultsBySubject.values()
                .stream()
                .map(subjectResults -> {

                    PracticeResult firstResult = subjectResults.getFirst();

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
