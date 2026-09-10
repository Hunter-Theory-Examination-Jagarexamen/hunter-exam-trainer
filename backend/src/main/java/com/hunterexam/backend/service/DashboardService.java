package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.DashboardResponse;
import com.hunterexam.backend.dto.RecentActivityResponse;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public DashboardResponse getDashboard(String email) {

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

    public List<RecentActivityResponse> getRecentActivity(String email) {

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
