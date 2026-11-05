package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.DashboardResponse;
import com.hunterexam.backend.dto.RecentActivityResponse;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.repository.projection.MockExamScore;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Fetch dashboard details.
 * <p>
 * The cards (mock exams, questions answered, average and best score) and the
 * recent mock exams. Mock exam figures come from scored mock exams only
 * (open or late exams don't count); "questions answered" counts every
 * answered question, practice and mock exams, from {@code SubjectStats}.
 */
@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final SubjectStatsRepository subjectStatsRepository;

    public DashboardService(
            UserRepository userRepository,
            AttemptQuestionRepository attemptQuestionRepository,
            SubjectStatsRepository subjectStatsRepository
    ) {
        this.userRepository = userRepository;
        this.attemptQuestionRepository = attemptQuestionRepository;
        this.subjectStatsRepository = subjectStatsRepository;
    }

    /**
     * Retrieves dashboard summary details for the given user.
     *
     * @param email user's email id
     * @return the dashboard response
     */
    public DashboardResponse getDashboard(String email) {

        User user = findUser(email);

        List<MockExamScore> exams = attemptQuestionRepository.findScoredMockExams(List.of(user));

        long averageScore = Math.round(
                exams.stream()
                        .mapToLong(MockExamScore::score)
                        .average()
                        .orElse(0)
        );

        long bestScore = exams.stream()
                .mapToLong(MockExamScore::score)
                .max()
                .orElse(0);

        return new DashboardResponse(
                exams.size(),
                subjectStatsRepository.countAnsweredByUser(user),
                averageScore,
                bestScore
        );
    }

    /**
     * The user's 5 most recent scored mock exams. The id is the exam's attempt id.
     *
     * @param email user's email id
     * @return the list of recent activity response
     */
    public List<RecentActivityResponse> getRecentActivity(String email) {

        User user = findUser(email);

        // The query returns newest first.
        return attemptQuestionRepository.findScoredMockExams(List.of(user)).stream()
                .limit(5)
                .map(exam -> new RecentActivityResponse(
                        exam.attemptId(),
                        exam.correct(),
                        exam.total(),
                        exam.score(),
                        exam.finishedAt()
                ))
                .toList();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
