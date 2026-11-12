package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.repository.projection.CountedAnswer;
import com.hunterexam.backend.repository.projection.SubjectQuestionCount;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The student's statistics, measured as mastery of the question bank.
 * <p>
 * A question is <b>mastered</b> when the student's latest answer to it is
 * correct; a later wrong answer makes it not mastered again. The numbers are
 * worked out from the stored answers on each request, nothing is saved.
 */
@Service
public class StatisticsService {

    /** Ranges up to this many weeks get one point per day, longer ones one per week. */
    private static final int MAX_WEEKS_WITH_DAILY_POINTS = 4;

    private final AttemptQuestionRepository attemptQuestionRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public StatisticsService(
            AttemptQuestionRepository attemptQuestionRepository,
            QuestionRepository questionRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository
    ) {
        this.attemptQuestionRepository = attemptQuestionRepository;
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }


    /**
     * The logged-in student's mastery per subject, for the Statistics page.
     *
     * @param email user's email id
     * @return one row per subject that has questions
     */
    public List<SubjectPerformanceResponse> getSubjectPerformance(String email) {

        User user = findUser(email);

        return toSubjectMastery(
                attemptQuestionRepository.findCountedAnswers(user),
                subjectRepository.findQuestionCounts());
    }

    /**
     * Mastery per subject: mastered questions / questions in the subject.
     * Subjects without answers yet are included (0 mastered); subjects without
     * questions are left out (nothing to master).
     * <p>
     * {@code static} because it only uses its arguments, so
     * {@code AdminService} can call it for Learner Progress with rows it
     * loaded itself, and a student and an admin always see the same numbers.
     *
     * @param answers  the student's counted answers, oldest first
     * @param subjects every subject with its number of questions
     * @return one row per subject with questions, in the order of {@code subjects}
     */
    public static List<SubjectPerformanceResponse> toSubjectMastery(
            List<CountedAnswer> answers,
            List<SubjectQuestionCount> subjects
    ) {
        // Count the mastered questions per subject id.
        Map<Long, Long> masteredPerSubject = latestAnswerPerQuestion(answers).values().stream()
                .filter(CountedAnswer::isCorrect)
                .collect(Collectors.groupingBy(CountedAnswer::subjectId, Collectors.counting()));

        return subjects.stream()
                .filter(subject -> subject.questionCount() > 0)
                .map(subject -> {
                    long mastered = masteredPerSubject.getOrDefault(subject.subjectId(), 0L);
                    long total = subject.questionCount();

                    return new SubjectPerformanceResponse(
                            subject.subjectId(),
                            subject.subjectName(),
                            percentage(mastered, total),
                            mastered,
                            total);
                })
                .toList();
    }

    /**
     * Each question's latest answer. The answers come oldest first, so a later
     * answer to the same question replaces the earlier one in the map.
     */
    static Map<Long, CountedAnswer> latestAnswerPerQuestion(List<CountedAnswer> answers) {
        Map<Long, CountedAnswer> latest = new HashMap<>();

        for (CountedAnswer answer : answers) {
            latest.put(answer.questionId(), answer);
        }
        return latest;
    }

    /**
     * The share of the whole question bank the student had mastered, for the
     * last {@code weeks} weeks: as it was at the end of each day the student
     * answered something, or, for ranges longer than
     * {@value #MAX_WEEKS_WITH_DAILY_POINTS} weeks, at the end of each such week.
     * <p>
     * Every answer is read, also those from before the range, so the first
     * point starts from what the student already knew.
     *
     * @param email user's email
     * @param weeks number of weeks to look back
     * @return the points, oldest first; days or weeks without answers have none
     */
    public List<ProgressOverTimeResponse> getProgressOverTime(String email, int weeks) {

        User user = findUser(email);

        LocalDate firstDay = LocalDate.now()
                .minusWeeks(weeks)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<ProgressOverTimeResponse> days =
                masteryPerDay(attemptQuestionRepository.findCountedAnswers(user), questionRepository.count())
                        .stream()
                        .filter(point -> !point.date().isBefore(firstDay))
                        .toList();

        return weeks <= MAX_WEEKS_WITH_DAILY_POINTS ? days : lastDayPerWeek(days);
    }

    /**
     * One point per day with answers: the mastery at the end of that day.
     * <p>
     * Goes through the answers in time order and keeps a running count of
     * mastered questions, updated by each answer (a question that was mastered
     * and is now answered wrong takes one away).
     *
     * @param answers        the student's counted answers, oldest first
     * @param totalQuestions questions in the whole bank
     * @return the points, oldest first
     */
    static List<ProgressOverTimeResponse> masteryPerDay(List<CountedAnswer> answers, long totalQuestions) {
        Map<Long, Boolean> masteredByQuestion = new HashMap<>();
        long mastered = 0;

        // A TreeMap keeps its keys sorted, so the days come out oldest first.
        // A later answer the same day replaces the day's point: the last one is the end of the day.
        Map<LocalDate, ProgressOverTimeResponse> byDay = new TreeMap<>();

        for (CountedAnswer answer : answers) {
            // put returns the question's previous value (null if never answered).
            Boolean wasMastered = masteredByQuestion.put(answer.questionId(), answer.isCorrect());

            if (Boolean.TRUE.equals(wasMastered)) {
                mastered--;
            }
            if (answer.isCorrect()) {
                mastered++;
            }

            LocalDate day = answer.answeredAt().toLocalDate();
            byDay.put(day, new ProgressOverTimeResponse(
                    day, percentage(mastered, totalQuestions), mastered, totalQuestions));
        }
        return new ArrayList<>(byDay.values());
    }

    /**
     * One point per week instead of per day: the last day of each week, dated
     * to the week's Monday. Mastery only builds on earlier answers, so the
     * last day's value is the value at the end of the week.
     *
     * @param days the points per day, oldest first
     * @return the points per week, oldest first
     */
    static List<ProgressOverTimeResponse> lastDayPerWeek(List<ProgressOverTimeResponse> days) {
        Map<LocalDate, ProgressOverTimeResponse> byWeek = new TreeMap<>();

        for (ProgressOverTimeResponse day : days) {
            LocalDate monday = day.date().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            byWeek.put(monday, new ProgressOverTimeResponse(
                    monday, day.percentage(), day.mastered(), day.total()));
        }
        return new ArrayList<>(byWeek.values());
    }

    /** Rounded percentage; 0 when there is nothing to count. */
    private static long percentage(long part, long whole) {
        return whole == 0 ? 0 : Math.round(part * 100.0 / whole);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
