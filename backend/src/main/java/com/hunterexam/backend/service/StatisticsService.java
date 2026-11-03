package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.PracticeResultRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
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
    private final SubjectStatsRepository subjectStatsRepository;
    private final UserRepository userRepository;

    public StatisticsService(
            PracticeResultRepository practiceResultRepository,
            SubjectStatsRepository subjectStatsRepository,
            UserRepository userRepository
    ) {
        this.practiceResultRepository = practiceResultRepository;
        this.subjectStatsRepository = subjectStatsRepository;
        this.userRepository = userRepository;
    }


    /**
     * The logged-in student's performance per subject, for the Statistics page.
     *
     * @param email user's email id
     * @return one row per subject the student has answered questions in
     */
    public List<SubjectPerformanceResponse> getSubjectPerformance(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return subjectPerformanceFor(user);
    }

    /**
     * Performance per subject: correct answers / answered questions, from
     * {@link SubjectStats}. Counts every answered question, from practice and
     * mock exams alike, so each answer weighs the same.
     * <p>
     * Shared by the Statistics page and the admin's Learner Progress, so a
     * student and an admin always see the same numbers.
     *
     * @param user the student
     * @return one row per subject with at least one answer, ordered by subject id
     */
    public List<SubjectPerformanceResponse> subjectPerformanceFor(User user) {

        return toSubjectPerformance(subjectStatsRepository.findAnsweredByUser(user));
    }

    /**
     * The shared calculation: one row per {@link SubjectStats}, with
     * correct / answered as a rounded percentage. {@code static} because it
     * only uses its argument, so {@code AdminService} can call it for rows it
     * loaded itself.
     *
     * @param subjectStats one student's totals, one per subject
     * @return one row per subject, in the same order
     */
    public static List<SubjectPerformanceResponse> toSubjectPerformance(List<SubjectStats> subjectStats) {

        return subjectStats.stream()
                .map(stats -> new SubjectPerformanceResponse(
                        stats.getSubject().getId(),
                        stats.getSubject().getName(),
                        percentage(stats.getCorrect(), stats.getAnswered())
                ))
                .toList();
    }

    /** Rounded percentage; 0 when nothing was answered. */
    private static long percentage(long correct, long answered) {
        return answered == 0 ? 0 : Math.round(correct * 100.0 / answered);
    }

    /**
     * Calculates the user's practice accuracy grouped by ISO week
     * (Monday–Sunday) for the last N weeks.
     *
     * <p>Only weeks in which the user practiced are included in the result,
     * ordered from oldest to newest.
     *
     * @param email user's email
     * @param weeks number of weeks to look back
     * @return list of weekly progress points
     */
    public List<ProgressOverTimeResponse> getProgressOverTime(String email, int weeks) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime since = LocalDateTime.now()
                .minusWeeks(weeks)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate()
                .atStartOfDay();

        List<PracticeResult> results =
                practiceResultRepository.findByUserAndCompletedAtAfter(user, since);

        // Group by the Monday of the week the result was completed.
        Map<LocalDate, List<PracticeResult>> byWeek = results.stream()
                .collect(Collectors.groupingBy(r ->
                        r.getCompletedAt().toLocalDate()
                                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                ));

        return byWeek.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    long correct = entry.getValue().stream()
                            .mapToLong(PracticeResult::getCorrectAnswers).sum();
                    long total = entry.getValue().stream()
                            .mapToLong(PracticeResult::getTotalQuestions).sum();
                    long accuracy = total == 0 ? 0 : Math.round(correct * 100.0 / total);

                    return new ProgressOverTimeResponse(entry.getKey(), accuracy, total);
                })
                .toList();
    }
}
