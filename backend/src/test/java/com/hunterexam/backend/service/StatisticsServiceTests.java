package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.repository.projection.CountedAnswer;
import com.hunterexam.backend.repository.projection.SubjectQuestionCount;
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
import static org.mockito.Mockito.when;

/**
 * Unit tests for the question-mastery numbers in {@link StatisticsService}.
 * The repositories are Mockito mocks, so no database is used.
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTests {

    private static final String EMAIL = "student@example.test";

    @Mock AttemptQuestionRepository attemptQuestionRepository;
    @Mock QuestionRepository questionRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock UserRepository userRepository;

    private StatisticsService statisticsService;
    private User user;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService(
                attemptQuestionRepository, questionRepository, subjectRepository, userRepository);
        user = new User();
        user.setEmail(EMAIL);
    }

    /** One counted answer: question {@code questionId} in subject {@code subjectId}. */
    private CountedAnswer answer(long questionId, long subjectId, LocalDateTime at, boolean correct) {
        return new CountedAnswer(questionId, subjectId, at, correct);
    }

    // ----- Mastery per subject -----

    @Test
    void masteryIsLatestAnswerPerSubjectWithTheBankAsDenominator() {
        // Arrange: Weapons has 4 questions; the latest answers are right for
        // 11 and 13, wrong for 12 (its earlier right answer was un-mastered)
        // and 14 — 2 of 4 mastered, 4 questions answered (12 twice). Wildlife has 5
        // questions and is untouched. The subject order is the bank's.
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findQuestionCounts()).thenReturn(List.of(
                new SubjectQuestionCount(1L, "Weapons", 4L),
                new SubjectQuestionCount(2L, "Wildlife", 5L)));
        when(attemptQuestionRepository.findCountedAnswers(user)).thenReturn(List.of(
                answer(11, 1, LocalDateTime.now().minusDays(2), true),
                answer(12, 1, LocalDateTime.now().minusDays(2), true),
                answer(13, 1, LocalDateTime.now().minusDays(2), true),
                answer(14, 1, LocalDateTime.now().minusDays(1), false),
                answer(12, 1, LocalDateTime.now().minusHours(1), false)));   // un-masters 12

        // Act
        List<SubjectPerformanceResponse> rows = statisticsService.getSubjectPerformance(EMAIL);

        // Assert: one row per subject, bank order; Weapons 2 of 4 = 50 % with
        // 4 questions answered, Wildlife 0 of 5 with no answers at all
        assertEquals(2, rows.size());

        SubjectPerformanceResponse weaponsRow = rows.get(0);
        assertEquals(1L, weaponsRow.getSubjectId());
        assertEquals("Weapons", weaponsRow.getSubjectName());
        assertEquals(2, weaponsRow.getMastered());
        assertEquals(4, weaponsRow.getTotal());
        assertEquals(4, weaponsRow.getAnswered());   // distinct questions, not answers
        assertEquals(50, weaponsRow.getPercentage());

        SubjectPerformanceResponse wildlifeRow = rows.get(1);
        assertEquals(5, wildlifeRow.getTotal());
        assertEquals(0, wildlifeRow.getMastered());
        assertEquals(0, wildlifeRow.getAnswered());
        assertEquals(0, wildlifeRow.getPercentage());
    }

    @Test
    void masteryRoundsHalfUpAndCountsEachQuestionOnce() {
        // Arrange: 2 of 3 questions mastered, even though question 1 was
        // answered correctly three times — repeats never count twice
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findQuestionCounts()).thenReturn(List.of(
                new SubjectQuestionCount(1L, "Weapons", 3L)));
        when(attemptQuestionRepository.findCountedAnswers(user)).thenReturn(List.of(
                answer(11, 1, LocalDateTime.now().minusDays(1), true),
                answer(11, 1, LocalDateTime.now().minusHours(3), true),
                answer(11, 1, LocalDateTime.now().minusHours(2), true),
                answer(12, 1, LocalDateTime.now().minusHours(1), true)));

        // Act + Assert: 2 of 3 = 66.67 %, rounds up to 67
        SubjectPerformanceResponse row =
                statisticsService.getSubjectPerformance(EMAIL).getFirst();
        assertEquals(2, row.getMastered());
        assertEquals(3, row.getTotal());
        assertEquals(67, row.getPercentage());
    }

    @Test
    void subjectWithoutQuestionsHasNoPercentage() {
        // Arrange: an empty subject still shows as a row, with nothing to master
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findQuestionCounts()).thenReturn(List.of(
                new SubjectQuestionCount(1L, "Weapons", 0L)));

        // Act + Assert
        SubjectPerformanceResponse row =
                statisticsService.getSubjectPerformance(EMAIL).getFirst();
        assertEquals(0, row.getTotal());
        assertEquals(0, row.getPercentage());
    }

    @Test
    void newUserWithNoAnswersGetsZeroMasteryRowsNotAnEmptyList() {
        // Arrange: a brand-new student in a bank that has subjects
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findQuestionCounts()).thenReturn(List.of(
                new SubjectQuestionCount(1L, "Weapons", 4L)));
        when(attemptQuestionRepository.findCountedAnswers(user)).thenReturn(List.of());

        // Act
        List<SubjectPerformanceResponse> rows = statisticsService.getSubjectPerformance(EMAIL);

        // Assert: the subject is listed with 0 mastered of 4, so the page can
        // show "no answers yet" instead of hiding the subject
        assertNotNull(rows, "must be a list, not null");
        assertEquals(1, rows.size());
        assertEquals(0, rows.getFirst().getMastered());
        assertEquals(4, rows.getFirst().getTotal());
        assertEquals(0, rows.getFirst().getAnswered());
        assertEquals(0, rows.getFirst().getPercentage());
    }

    // ----- Progress over time -----

    /** Monday of the current week, so test answers never fall on a week edge by accident. */
    private static final LocalDate THIS_MONDAY =
            LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    @Test
    void progressIsOnePointPerDayOldestFirstWithMasteryOfTheWholeBank() {
        // Arrange: 4 questions in the bank; the student masters 2 of them
        // before the window (so the first day carries them in) and a third one
        // today
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(questionRepository.count()).thenReturn(4L);
        when(attemptQuestionRepository.findCountedAnswers(user)).thenReturn(List.of(
                answer(11, 1, THIS_MONDAY.minusWeeks(3).atTime(10, 0), true),
                answer(12, 1, THIS_MONDAY.minusWeeks(3).atTime(10, 1), true),
                answer(13, 1, LocalDate.now().atTime(9, 0), true)));

        // Act
        List<ProgressOverTimeResponse> points = statisticsService.getProgressOverTime(EMAIL, 2);

        // Assert: one point per calendar day from the Monday of last week (2
        // calendar weeks) to today, oldest first; days without answers repeat
        // the previous day's state
        LocalDate from = THIS_MONDAY.minusWeeks(1);
        LocalDate today = LocalDate.now();
        long expectedDays = from.datesUntil(today.plusDays(1)).count();
        assertEquals(expectedDays, points.size());

        ProgressOverTimeResponse first = points.getFirst();
        assertEquals(from, first.date());
        assertEquals(50, first.percentage());      // 2 of 4 carried in
        assertEquals(2, first.mastered());
        assertEquals(4, first.total());
        assertEquals(50, points.get(1).percentage());  // still 2 of 4

        ProgressOverTimeResponse last = points.getLast();
        assertEquals(today, last.date());
        assertEquals(75, last.percentage());       // 3 of 4 after today's answer
        assertEquals(3, last.mastered());
    }

    @Test
    void progressFollowsTheLatestAnswerOfEachQuestion() {
        // Arrange: one question, mastered, then lost, then mastered again on
        // three different days inside the window — the series must dip, not
        // just rise
        LocalDate from = THIS_MONDAY.minusWeeks(1);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(questionRepository.count()).thenReturn(1L);
        when(attemptQuestionRepository.findCountedAnswers(user)).thenReturn(List.of(
                answer(11, 1, from.atTime(9, 0), true),
                answer(11, 1, from.plusDays(1).atTime(9, 0), false),
                answer(11, 1, from.plusDays(2).atTime(9, 0), true)));

        // Act
        List<ProgressOverTimeResponse> points = statisticsService.getProgressOverTime(EMAIL, 2);

        // Assert: 100 % → 0 % → 100 %, and days without answers hold the value
        assertEquals(100, points.getFirst().percentage());
        assertEquals(0, points.get(1).percentage());
        assertEquals(100, points.get(2).percentage());
        assertEquals(100, points.getLast().percentage());
    }

    @Test
    void progressIsEmptyWithoutAnswers() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptQuestionRepository.findCountedAnswers(user)).thenReturn(List.of());

        assertTrue(statisticsService.getProgressOverTime(EMAIL, 4).isEmpty());
    }

    @Test
    void progressIsEmptyWhenTheBankIsEmpty() {
        // Arrange: answers exist, but there is nothing to master — no
        // percentage and no division by zero
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptQuestionRepository.findCountedAnswers(user)).thenReturn(List.of(
                answer(11, 1, LocalDateTime.now().minusDays(1), true)));
        when(questionRepository.count()).thenReturn(0L);

        assertTrue(statisticsService.getProgressOverTime(EMAIL, 4).isEmpty());
    }
}
