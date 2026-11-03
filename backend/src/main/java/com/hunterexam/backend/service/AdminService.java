package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.AdminStatsResponse;
import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.dto.MockExamSummary;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.PracticeResultRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
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
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectStatsRepository subjectStatsRepository;

    public AdminService(
            UserRepository userRepository,
            PracticeResultRepository practiceResultRepository,
            ExamResultRepository examResultRepository,
            QuestionRepository questionRepository,
            SubjectRepository subjectRepository,
            SubjectStatsRepository subjectStatsRepository
    ) {
        this.userRepository = userRepository;
        this.practiceResultRepository = practiceResultRepository;
        this.examResultRepository = examResultRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.subjectStatsRepository = subjectStatsRepository;
    }

    /**
     * Counts for the overview cards. Students are counted by role only, so
     * accounts that cannot use password login are included.
     */
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                questionRepository.count(),
                subjectRepository.count(),
                userRepository.countByRole(Role.STUDENT)
        );
    }

    public List<LearnerProgressResponse> getLearnerProgress() {

        List<User> learners = userRepository.findByRole(Role.STUDENT);

        // Every student's subject totals in one query, then split per student
        // (by user id), so the page doesn't run one query per student.
        Map<Long, List<SubjectStats>> statsByLearner = learners.isEmpty()
                ? Map.of()
                : subjectStatsRepository.findAnsweredByUserIn(learners).stream()
                        .collect(Collectors.groupingBy(stats -> stats.getUser().getId()));

        return learners.stream()
                .sorted((a, b) -> a.getFullName().compareToIgnoreCase(b.getFullName()))
                .map(learner -> buildLearnerProgress(
                        learner,
                        statsByLearner.getOrDefault(learner.getId(), List.of())))
                .toList();
    }

    private LearnerProgressResponse buildLearnerProgress(User learner, List<SubjectStats> subjectStats) {

        List<PracticeResult> practiceResults =
                practiceResultRepository.findByUser(learner);

        List<ExamResult> examResults =
                examResultRepository.findByUserOrderByCompletedAtDesc(learner);

        // Same calculation as the student's own Statistics page; weakest subject first.
        List<SubjectPerformanceResponse> perSubject =
                StatisticsService.toSubjectPerformance(subjectStats).stream()
                        .sorted(Comparator.comparingLong(SubjectPerformanceResponse::getPercentage))
                        .toList();
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