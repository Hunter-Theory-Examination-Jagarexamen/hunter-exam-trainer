package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.DashboardResponse;
import com.hunterexam.backend.dto.RecentActivityResponse;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Fetch dashboard details.
 * <p>
 * This service fetch the dashboard data's,
 * including mock exams, questions answered, score, correct answers count,
 * incorrect answers count, total questions and completed date.
 */
@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ExamResultRepository examResultRepository;

    public DashboardService(
            UserRepository userRepository,
            ExamResultRepository examResultRepository
    ) {
        this.userRepository = userRepository;
        this.examResultRepository = examResultRepository;
    }

    /**
     * Retrieves dashboard summary details for the given user.
     * <p>
     * Looks up the user by email and aggregates their mock exam results
     * (total exams, questions answered, average score, best score).
     * <p>
     * @param email user's email id
     * @return the dashboard response
     */
    public DashboardResponse getDashboard(String email) {

        // Find the user using the email provided during login.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ExamResult> results = examResultRepository.findByUserOrderByCompletedAtDesc(user);

        long mockExams = results.size();

        long questionsAnswered = results.stream()
                .mapToLong(result ->
                        result.getCorrectAnswers() + result.getIncorrectAnswers())
                .sum();

        long averageScore = results.isEmpty()
                ? 0
                : Math.round(
                        results.stream()
                                .mapToInt(ExamResult::getScore)
                                .average()
                                .orElse(0)
                );

        long bestScore = results.stream()
                .mapToLong(ExamResult::getScore)
                .max()
                .orElse(0);

        return new DashboardResponse(
                mockExams,
                questionsAnswered,
                averageScore,
                bestScore
        );
    }

    /**
     * Retrieves the user's most recent mock exam activity.
     * <p>
     * Looks up the user by email and returns their last 5 completed exam results.
     * <p>
     * @param email user's email id
     * @return the list of recent activity response
     */
    public List<RecentActivityResponse> getRecentActivity(String email) {

        // Find the user using the email provided during login.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ExamResult> results = examResultRepository.findByUserOrderByCompletedAtDesc(user);

        return results.stream()
                .limit(5)
                .map(result -> new RecentActivityResponse(
                        result.getId(),
                        result.getCorrectAnswers(),
                        result.getTotalQuestions(),
                        result.getScore(),
                        result.getCompletedAt()
                ))
                .toList();
    }
}
