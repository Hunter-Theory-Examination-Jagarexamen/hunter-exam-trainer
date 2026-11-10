package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.repository.projection.AnswerOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the "performance by subject" numbers in {@link StatisticsService}.
 * The repositories are Mockito mocks, so no database is used.
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTests {

    private static final String EMAIL = "student@example.test";

    @Mock AttemptQuestionRepository attemptQuestionRepository;
    @Mock SubjectStatsRepository subjectStatsRepository;
    @Mock UserRepository userRepository;

    private StatisticsService statisticsService;
    private User user;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService(attemptQuestionRepository, subjectStatsRepository, userRepository);
        user = new User();
        user.setEmail(EMAIL);
    }

    private Subject subject(long id, String name) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setName(name);
        return subject;
    }

    /** The student's running totals in one subject. */
    private SubjectStats stats(Subject subject, long answered, long correct) {
        SubjectStats stats = new SubjectStats(user, subject);
        stats.setAnswered(answered);
        stats.setCorrect(correct);
        return stats;
    }

    // ----- Performance by subject -----

    @Test
    void percentageIsCorrectAnswersOutOfAnsweredPerSubject() {
        // Arrange: Weapons 17 of 20 correct, Wildlife 1 of 3 (33.3 %, rounds down)
        Subject weapons = subject(1, "Weapons");
        Subject wildlife = subject(2, "Wildlife");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectStatsRepository.findAnsweredByUser(user.getId())).thenReturn(List.of(
                stats(weapons, 20, 17),
                stats(wildlife, 3, 1)));

        // Act
        List<SubjectPerformanceResponse> rows = statisticsService.getSubjectPerformance(EMAIL);

        // Assert: one row per subject, in the repository's order
        assertEquals(2, rows.size());
        assertEquals(1L, rows.get(0).getSubjectId());
        assertEquals("Weapons", rows.get(0).getSubjectName());
        assertEquals(85, rows.get(0).getPercentage());
        assertEquals(33, rows.get(1).getPercentage());
    }

    @Test
    void percentageRoundsHalfUp() {
        // Arrange: 2 of 3 correct = 66.67 %
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectStatsRepository.findAnsweredByUser(user.getId())).thenReturn(List.of(
                stats(subject(1, "Weapons"), 3, 2)));

        // Act + Assert
        assertEquals(67, statisticsService.getSubjectPerformance(EMAIL).getFirst().getPercentage());
    }

    @Test
    void newUserWithNoAnswersGetsAnEmptyList() {
        // Arrange: a brand-new student who hasn't answered anything yet
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectStatsRepository.findAnsweredByUser(user.getId())).thenReturn(List.of());

        // Act
        List<SubjectPerformanceResponse> rows = statisticsService.getSubjectPerformance(EMAIL);

        // Assert: an empty list (not null, no crash, no made-up 0 % rows),
        // so the statistics page can show "no results yet"
        assertNotNull(rows, "must be an empty list, not null");
        assertTrue(rows.isEmpty());
    }

    // ----- Progress over time -----

    /** Monday of the current week, so test answers never fall on a week edge by accident. */
    private static final LocalDate THIS_MONDAY =
            LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    private AnswerOutcome answer(LocalDateTime at, boolean correct) {
        return new AnswerOutcome(at, correct);
    }

    @Test
    void progressGroupsAnswersByWeekOldestFirst() {
        // Arrange: last week 1 of 4 right; this week 3 of 3 right (Monday and Wednesday)
        LocalDateTime lastWeek = THIS_MONDAY.minusWeeks(1).atTime(10, 0);
        LocalDateTime thisWeek = THIS_MONDAY.atTime(10, 0);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptQuestionRepository.findCountedAnswersSince(eq(user), any())).thenReturn(List.of(
                answer(lastWeek, true),
                answer(lastWeek.plusMinutes(1), false),
                answer(lastWeek.plusMinutes(2), false),
                answer(lastWeek.plusDays(3), false),     // Thursday, same week
                answer(thisWeek, true),
                answer(thisWeek.plusDays(2), true),
                answer(thisWeek.plusDays(2).plusMinutes(1), true)));

        // Act
        List<ProgressOverTimeResponse> weeks = statisticsService.getProgressOverTime(EMAIL, 4);

        // Assert: one point per week, keyed by its Monday, oldest first
        assertEquals(2, weeks.size());
        assertEquals(THIS_MONDAY.minusWeeks(1), weeks.get(0).weekStart());
        assertEquals(25, weeks.get(0).accuracy());
        assertEquals(4, weeks.get(0).questionsAnswered());
        assertEquals(THIS_MONDAY, weeks.get(1).weekStart());
        assertEquals(100, weeks.get(1).accuracy());
        assertEquals(3, weeks.get(1).questionsAnswered());
    }

    @Test
    void progressIsEmptyWithoutAnswers() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertTrue(statisticsService.getProgressOverTime(EMAIL, 4).isEmpty());
    }
}
