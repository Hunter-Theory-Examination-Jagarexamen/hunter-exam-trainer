package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.repository.projection.AnswerOutcome;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Handles calculation of practice performance statistics.
 * <p>
 * This service aggregates a user's practice results by subject and
 * calculates the average score per subject, used for the Statistics page.
 */
@Service
public class StatisticsService {

    private final AttemptQuestionRepository attemptQuestionRepository;
    private final SubjectStatsRepository subjectStatsRepository;
    private final UserRepository userRepository;

    public StatisticsService(
            AttemptQuestionRepository attemptQuestionRepository,
            SubjectStatsRepository subjectStatsRepository,
            UserRepository userRepository
    ) {
        this.attemptQuestionRepository = attemptQuestionRepository;
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
        Long userId = user.getId();

        return toSubjectPerformance(subjectStatsRepository.findAnsweredByUser(userId));
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
     * Calculates the user's accuracy (correct / answered) grouped by ISO week
     * (Monday–Sunday) for the last N weeks, from each stored answer: practice
     * and scored mock exams, the same answers as {@code SubjectStats}.
     *
     * <p>Only weeks in which the user answered questions are included in the
     * result, ordered from oldest to newest.
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

        List<AnswerOutcome> answers =
                attemptQuestionRepository.findCountedAnswersSince(user, since);

        // Group by the Monday of the week each answer was given. A TreeMap keeps
        // its keys sorted, so the weeks come out oldest first.
        Map<LocalDate, List<AnswerOutcome>> byWeek = answers.stream()
                .collect(Collectors.groupingBy(
                        answer -> answer.answeredAt().toLocalDate()
                                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        TreeMap::new,
                        Collectors.toList()));

        return byWeek.entrySet().stream()
                .map(entry -> {
                    long total = entry.getValue().size();
                    long correct = entry.getValue().stream()
                            .filter(AnswerOutcome::isCorrect)
                            .count();

                    return new ProgressOverTimeResponse(entry.getKey(), percentage(correct, total), total);
                })
                .toList();
    }
}
