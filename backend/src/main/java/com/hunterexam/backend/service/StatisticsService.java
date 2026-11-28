package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.Subject;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Question-mastery statistics, shared by the student's Statistics page and the
 * admin's Learner Progress, so both always show the same numbers.
 *
 * <h2>What counts as mastered</h2>
 * A question is mastered when the student's <b>latest counted answer</b> to it
 * is correct: a first correct answer masters it, a later wrong answer un-
 * masters it, a later correct answer masters it again, and an unanswered
 * question is never mastered. Repeated correct answers never count twice —
 * each question contributes at most one mastered question. Answers are ordered
 * by {@code answered_at}, with the persisted answer id as a stable tie-break
 * for equal timestamps, so "latest" is well defined.
 *
 * <h2>Which answers count</h2>
 * Exactly the eligibility rule the statistics have always used, held in one
 * place by {@code AttemptQuestionRepository.findCountedAnswers}: every practice
 * answer, plus a mock exam's answers when the exam was scored, or when it
 * ended without a result and the answer was given inside the exam's answer
 * window (start + 60 minutes + 2 minutes grace, #187). Answers of exams still
 * open and answers given after that window never count.
 *
 * <h2>Denominators</h2>
 * Per subject: the subject's questions in the bank <b>right now</b>, whether or
 * not the student has answered them, so a subject with no answers reads
 * "0 mastered of N" and a subject with no questions has no percentage (0 %,
 * shown as unavailable by the pages). Progress over time: the size of the
 * whole bank right now. The schema stores no question creation dates, so
 * historical points cannot know how big the bank was back then — adding
 * questions can lower an old point's percentage. That tradeoff is documented
 * in the API specification rather than papered over with invented history.
 *
 * <h2>Time zone</h2>
 * Days are calendar days in the server's default time zone, the same zone the
 * stored {@code answered_at} values are read in; a day's value is the state at
 * the last instant of that day.
 */
@Service
public class StatisticsService {

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
     * The logged-in student's question mastery per subject, for the Statistics
     * page.
     *
     * @param email user's email id
     * @return one row per subject in the bank, ordered by subject id
     */
    public List<SubjectPerformanceResponse> getSubjectPerformance(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return subjectPerformanceFor(user);
    }

    /**
     * Question mastery per subject: mastered questions ÷ questions in the
     * subject, for every subject in the bank.
     * <p>
     * Shared by the Statistics page and the admin's Learner Progress, so a
     * student and a course leader always see the same numbers. Subjects the
     * student has not answered are included with 0 mastered; subjects without
     * questions are included with a total of 0 (no percentage).
     *
     * @param user the student
     * @return one row per subject, ordered by subject id
     */
    public List<SubjectPerformanceResponse> subjectPerformanceFor(User user) {
        List<CountedAnswer> answers = attemptQuestionRepository.findCountedAnswers(user);
        Map<Long, CountedAnswer> latest = latestAnswerPerQuestion(answers);

        Map<Long, Long> totalBySubject = new HashMap<>();
        for (SubjectQuestionCount count : questionRepository.countGroupedBySubject()) {
            totalBySubject.put(count.subjectId(), count.total());
        }

        // A mastered question is by definition an answered one, so its subject
        // comes from the answer itself; unanswered questions never contribute.
        Map<Long, Long> masteredBySubject = new HashMap<>();
        for (CountedAnswer answer : latest.values()) {
            if (answer.isCorrect()) {
                masteredBySubject.merge(answer.subjectId(), 1L, Long::sum);
            }
        }

        Map<Long, Long> answeredBySubject = new HashMap<>();
        for (CountedAnswer answer : answers) {
            answeredBySubject.merge(answer.subjectId(), 1L, Long::sum);
        }

        return subjectRepository.findAllByOrderById().stream()
                .map(subject -> {
                    long total = totalBySubject.getOrDefault(subject.getId(), 0L);
                    long mastered = masteredBySubject.getOrDefault(subject.getId(), 0L);
                    long answered = answeredBySubject.getOrDefault(subject.getId(), 0L);

                    return new SubjectPerformanceResponse(
                            subject.getId(),
                            subject.getName(),
                            mastered,
                            total,
                            answered,
                            percentage(mastered, total)
                    );
                })
                .toList();
    }

    /**
     * The share of the whole question bank the student had mastered at the end
     * of each day of the last {@code weeks} weeks, oldest first — one point per
     * calendar day, including days without answers (those keep the previous
     * day's state).
     * <p>
     * The whole answer history is read once and replayed in time order, so a
     * day's value is the state <em>at that day</em> and not today's state
     * back-dated: question A right → wrong → right gives 1 → 0 → 1.
     *
     * @param email user's email
     * @param weeks number of weeks to look back (the window starts on the
     *              Monday of that week)
     * @return one point per day in the window, oldest first; empty when the
     *         student has answered nothing or the bank is empty
     */
    public List<ProgressOverTimeResponse> getProgressOverTime(String email, int weeks) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return progressOverTimeFor(user, weeks);
    }

    /**
     * Same as {@link #getProgressOverTime(String, int)} for a user that is
     * already loaded — the admin's Learner Progress shares this calculation.
     */
    public List<ProgressOverTimeResponse> progressOverTimeFor(User user, int weeks) {
        List<CountedAnswer> answers = attemptQuestionRepository.findCountedAnswers(user);
        if (answers.isEmpty()) {
            return List.of();
        }

        long totalQuestions = questionRepository.count();
        if (totalQuestions == 0) {
            // Nothing to be master of: no percentage, no division by zero.
            return List.of();
        }

        LocalDate from = LocalDate.now()
                .minusWeeks(weeks)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate today = LocalDate.now();

        Map<Long, Boolean> mastered = new HashMap<>();
        long masteredCount = 0;
        int index = 0;

        // Answers given before the window decide the first day's state.
        while (index < answers.size()
                && answers.get(index).answeredAt().toLocalDate().isBefore(from)) {
            masteredCount = apply(answers.get(index++), mastered, masteredCount);
        }

        List<ProgressOverTimeResponse> points = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(today); day = day.plusDays(1)) {
            LocalDateTime endOfDay = day.atTime(LocalTime.MAX);

            // Every answer up to and including the end of this day, in the
            // order it was given; days without answers change nothing.
            while (index < answers.size()
                    && !answers.get(index).answeredAt().isAfter(endOfDay)) {
                masteredCount = apply(answers.get(index++), mastered, masteredCount);
            }

            points.add(new ProgressOverTimeResponse(
                    day,
                    percentage(masteredCount, totalQuestions),
                    masteredCount,
                    totalQuestions
            ));
        }

        return points;
    }

    /**
     * Folds one answer into the latest-answer-per-question state and returns
     * the new mastered count: a first correct answer adds one, a later wrong
     * answer on a mastered question removes one, a later correct answer on an
     * unmastered question adds one, everything else leaves the count alone.
     */
    private static long apply(CountedAnswer answer, Map<Long, Boolean> latest, long masteredCount) {
        boolean wasMastered = Boolean.TRUE.equals(latest.put(answer.questionId(), answer.isCorrect()));

        if (answer.isCorrect() && !wasMastered) {
            return masteredCount + 1;
        }
        if (!answer.isCorrect() && wasMastered) {
            return masteredCount - 1;
        }
        return masteredCount;
    }

    /**
     * The last answer of each question, keeping the repository's order, so
     * equal timestamps resolve by the persisted answer id.
     */
    private static Map<Long, CountedAnswer> latestAnswerPerQuestion(List<CountedAnswer> answers) {
        Map<Long, CountedAnswer> latest = new LinkedHashMap<>();
        for (CountedAnswer answer : answers) {
            latest.put(answer.questionId(), answer);
        }
        return latest;
    }

    /** Rounded percentage; 0 when there is nothing to divide by. */
    private static long percentage(long part, long whole) {
        return whole == 0 ? 0 : Math.round(part * 100.0 / whole);
    }
}
