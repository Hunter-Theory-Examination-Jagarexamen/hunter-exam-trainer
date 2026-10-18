package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.dto.MockExamSummary;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.PracticeResultRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminService {

    private static final int PASS_CORRECT_ANSWERS = 60;
    private static final int EXAM_TOTAL_QUESTIONS = 70;

    private final UserRepository userRepository;
    private final PracticeResultRepository practiceResultRepository;
    private final ExamResultRepository examResultRepository;

    public AdminService(
            UserRepository userRepository,
            PracticeResultRepository practiceResultRepository,
            ExamResultRepository examResultRepository
    ) {
        this.userRepository = userRepository;
        this.practiceResultRepository = practiceResultRepository;
        this.examResultRepository = examResultRepository;
    }

    public List<LearnerProgressResponse> getLearnerProgress() {

        List<User> learners = userRepository.findByRole(Role.STUDENT);

        return learners.stream()
                .sorted((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()))
                .map(this::buildLearnerProgress)
                .toList();
    }

    private LearnerProgressResponse buildLearnerProgress(User learner) {

        List<PracticeResult> practiceResults =
                practiceResultRepository.findByUser(learner);

        List<ExamResult> examResults =
                examResultRepository.findByUserOrderByCompletedAtDesc(learner);

        List<SubjectPerformanceResponse> perSubject = aggregateSubjects(practiceResults);
        MockExamSummary mockExams = aggregateMockExams(examResults);
        LocalDateTime lastActivity = computeLastActivity(practiceResults, examResults);

        return new LearnerProgressResponse(
                learner.getId(),
                learner.getFullName(),
                learner.getEmail(),
                learner.getCreatedAt(),
                lastActivity,
                perSubject,
                mockExams
        );
    }

    private List<SubjectPerformanceResponse> aggregateSubjects(List<PracticeResult> results) {

        Map<Long, List<PracticeResult>> bySubject = results.stream()
                .collect(Collectors.groupingBy(r -> r.getSubject().getId()));

        return bySubject.values().stream()
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
                .sorted(Comparator.comparingLong(SubjectPerformanceResponse::getPercentage))
                .toList();
    }

    private MockExamSummary aggregateMockExams(List<ExamResult> examResults) {

        if (examResults.isEmpty()) {
            return MockExamSummary.empty();
        }

        // Repository returns results ordered by completedAt DESC.
        ExamResult latest = examResults.getFirst();

        int bestScore = examResults.stream()
                .mapToInt(ExamResult::getScore)
                .max()
                .orElse(0);

        boolean latestPassed =
                latest.getTotalQuestions() == EXAM_TOTAL_QUESTIONS
                        && latest.getCorrectAnswers() >= PASS_CORRECT_ANSWERS;

        return new MockExamSummary(
                examResults.size(),
                latest.getScore(),
                latest.getCompletedAt(),
                bestScore,
                latestPassed
        );
    }

    private LocalDateTime computeLastActivity(
            List<PracticeResult> practiceResults,
            List<ExamResult> examResults
    ) {
        Stream<LocalDateTime> practiceTimes = practiceResults.stream()
                .map(PracticeResult::getCompletedAt);
        Stream<LocalDateTime> examTimes = examResults.stream()
                .map(ExamResult::getCompletedAt);

        return Stream.concat(practiceTimes, examTimes)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}