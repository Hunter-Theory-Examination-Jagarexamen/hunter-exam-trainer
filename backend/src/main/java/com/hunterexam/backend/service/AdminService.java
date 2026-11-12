package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.AdminStatsResponse;
import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.dto.LearnerSummaryResponse;
import com.hunterexam.backend.dto.MockExamSummary;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.FeedbackRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.repository.projection.MockExamScore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class AdminService {

    private static final int PASS_CORRECT_ANSWERS = 60;
    private static final int EXAM_TOTAL_QUESTIONS = 70;

    private final UserRepository userRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectStatsRepository subjectStatsRepository;
    private final FeedbackRepository feedbackRepository;

    public AdminService(
            UserRepository userRepository,
            AttemptQuestionRepository attemptQuestionRepository,
            QuestionRepository questionRepository,
            SubjectRepository subjectRepository,
            SubjectStatsRepository subjectStatsRepository,
            FeedbackRepository feedbackRepository
    ) {
        this.userRepository = userRepository;
        this.attemptQuestionRepository = attemptQuestionRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.subjectStatsRepository = subjectStatsRepository;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * Counts for the overview cards. Students are counted by role only, so
     * accounts that cannot use password login are included.
     */
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                questionRepository.count(),
                subjectRepository.count(),
                userRepository.countByRole(Role.STUDENT),
                feedbackRepository.countByHandledFalse()
        );
    }

    /**
     * Every user for the admin's list: admins first, then students, each group by name.
     */
    public List<LearnerSummaryResponse> getLearners() {
        return userRepository.findAll().stream()
                .sorted(Comparator
                        .comparingInt((User user) -> user.getRole() == Role.ADMIN ? 0 : 1)
                        .thenComparing(User::getFullName, String.CASE_INSENSITIVE_ORDER))
                .map(user -> new LearnerSummaryResponse(
                        user.getId(), user.getFullName(), user.getRole()))
                .toList();
    }

    /**
     * Progress for one user (student or admin), for the admin's detail view.
     *
     * @throws NotFoundException if no user has this id
     */
    public LearnerProgressResponse getLearnerProgress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // The repository queries take a list of users; here it's a list of one.
        List<User> oneUser = List.of(user);
        return buildLearnerProgress(
                user,
                subjectStatsRepository.findAnsweredByUserIn(oneUser),
                attemptQuestionRepository.findScoredMockExams(oneUser));
    }

    private LearnerProgressResponse buildLearnerProgress(
            User learner,
            List<SubjectStats> subjectStats,
            List<MockExamScore> examScores
    ) {

        // Same calculation as the student's own Statistics page; weakest subject first.
        List<SubjectPerformanceResponse> perSubject =
                StatisticsService.toSubjectMastery(
                                attemptQuestionRepository.findCountedAnswers(learner),
                                subjectRepository.findQuestionCounts())
                        .stream()
                        .sorted(Comparator.comparingLong(SubjectPerformanceResponse::getPercentage))
                        .toList();
        MockExamSummary mockExams = aggregateMockExams(examScores);
        LocalDateTime lastActivity = computeLastActivity(subjectStats, examScores);

        return new LearnerProgressResponse(
                learner.getId(),
                learner.getFullName(),
                learner.getEmail(),
                learner.getRole(),
                learner.getCreatedAt(),
                lastActivity,
                perSubject,
                mockExams
        );
    }

    /**
     * Count, latest, best and pass/fail from the student's scored mock exams.
     *
     * @param examScores scored mock exams, newest first (as the query returns them)
     */
    private MockExamSummary aggregateMockExams(List<MockExamScore> examScores) {

        if (examScores.isEmpty()) {
            return MockExamSummary.empty();
        }

        MockExamScore latest = examScores.getFirst();

        long bestScore = examScores.stream()
                .mapToLong(MockExamScore::score)
                .max()
                .orElse(0);

        boolean latestPassed =
                latest.total() == EXAM_TOTAL_QUESTIONS
                        && latest.correct() >= PASS_CORRECT_ANSWERS;

        return new MockExamSummary(
                examScores.size(),
                (int) latest.score(),
                latest.finishedAt(),
                (int) bestScore,
                latestPassed
        );
    }

    /**
     * The student's latest answer (practice or mock exam, from the subject
     * totals) or latest mock exam submit, whichever is newest. An exam's
     * answers are given before it is submitted, so its {@code finishedAt}
     * is usually the later one.
     *
     * @return null when the student has no answers and no exams
     */
    private LocalDateTime computeLastActivity(
            List<SubjectStats> subjectStats,
            List<MockExamScore> examScores
    ) {
        Stream<LocalDateTime> answerTimes = subjectStats.stream()
                .map(SubjectStats::getLastAnsweredAt);
        Stream<LocalDateTime> examTimes = examScores.stream()
                .map(MockExamScore::finishedAt);

        return Stream.concat(answerTimes, examTimes)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}
