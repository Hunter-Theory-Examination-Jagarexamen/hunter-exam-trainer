package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.PracticeResultRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PracticeResultRepository practiceResultRepository;

    public AdminService(UserRepository userRepository,
                        PracticeResultRepository practiceResultRepository) {
        this.userRepository = userRepository;
        this.practiceResultRepository = practiceResultRepository;
    }

    /**
     * Returns progress information for every non-admin learner.
     *
     * @return list of learner progress entries, ordered by name
     */
    public List<LearnerProgressResponse> getLearnerProgress() {

        List<User> learners = userRepository.findByRole(Role.STUDENT);

        return learners.stream()
                .sorted((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()))
                .map(this::buildLearnerProgress)
                .toList();
    }

    private LearnerProgressResponse buildLearnerProgress(User learner) {
        List<PracticeResult> results =
                practiceResultRepository.findByUser(learner);

        // Reuse the same aggregation the user's own statistics uses.
        Map<Long, List<PracticeResult>> bySubject = results.stream()
                .collect(Collectors.groupingBy(r -> r.getSubject().getId()));

        List<SubjectPerformanceResponse> perSubject = bySubject.values().stream()
                .map(group -> {
                    PracticeResult first = group.getFirst();
                    long avg = Math.round(
                            group.stream().mapToLong(PracticeResult::getScore).average().orElse(0)
                    );
                    return new SubjectPerformanceResponse(
                            first.getSubject().getId(),
                            first.getSubject().getName(),
                            avg
                    );
                })
                .sorted((a, b) -> Long.compare(a.getPercentage(), b.getPercentage()))  // weakest first
                .toList();

        LocalDateTime lastPracticedAt = results.stream()
                .map(PracticeResult::getCompletedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return new LearnerProgressResponse(
                learner.getId(),
                learner.getFullName(),
                learner.getEmail(),
                learner.getCreatedAt(),
                lastPracticedAt,
                perSubject
        );
    }
}