package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.repository.projection.CountedAnswer;
import com.hunterexam.backend.repository.projection.SubjectQuestionCount;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the question-mastery calculations in {@link StatisticsService}.
 * The {@code static} methods are called directly with made-up answers and
 * fixed dates: no database, no mocks, and the result never depends on the day
 * the tests run. The database side is covered by
 * {@link QuestionMasteryStatisticsTests}.
 */
class StatisticsServiceTests {

    /** A Monday, so a 7-day run starting here is one whole week. */
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);

    /** One counted answer to question {@code questionId} in subject 1, at noon on {@code day}. */
    private static CountedAnswer answer(long questionId, LocalDate day, boolean correct) {
        return new CountedAnswer(questionId, 1L, day.atTime(12, 0), correct);
    }

    private static SubjectQuestionCount subject(long id, String name, long questions) {
        return new SubjectQuestionCount(id, name, questions);
    }

    // ----- Mastery per subject: toSubjectMastery -----

    @Test
    void latestAnswerDecidesSoAWrongAnswerUnmasters() {
        // Arrange: question 11 right, then wrong; question 12 right
        List<CountedAnswer> answers = List.of(
                answer(11, MONDAY, true),
                answer(12, MONDAY, true),
                answer(11, MONDAY.plusDays(1), false));

        // Act
        SubjectPerformanceResponse row = StatisticsService
                .toSubjectMastery(answers, List.of(subject(1, "Weapons", 4)))
                .getFirst();

        // Assert: only 12 is still mastered
        assertEquals(1, row.getMastered());
    }

    @Test
    void repeatedCorrectAnswersCountOnceAndThePercentageRounds() {
        // Arrange: question 11 right three times, question 12 right once
        List<CountedAnswer> answers = List.of(
                answer(11, MONDAY, true),
                answer(11, MONDAY.plusDays(1), true),
                answer(11, MONDAY.plusDays(2), true),
                answer(12, MONDAY.plusDays(2), true));

        // Act
        SubjectPerformanceResponse row = StatisticsService
                .toSubjectMastery(answers, List.of(subject(1, "Weapons", 3)))
                .getFirst();

        // Assert: 2 of 3 = 66.67 %, rounded to 67
        assertEquals(2, row.getMastered());
        assertEquals(3, row.getTotal());
        assertEquals(67, row.getPercentage());
    }

    @Test
    void answeredCountsDistinctQuestionsNotAnswers() {
        // Arrange: question 11 answered three times, question 12 once
        List<CountedAnswer> answers = List.of(
                answer(11, MONDAY, false),
                answer(11, MONDAY.plusDays(1), false),
                answer(11, MONDAY.plusDays(2), true),
                answer(12, MONDAY.plusDays(2), false));

        // Act
        SubjectPerformanceResponse row = StatisticsService
                .toSubjectMastery(answers, List.of(subject(1, "Weapons", 4)))
                .getFirst();

        // Assert: 2 questions answered, not 4 answers
        assertEquals(2, row.getAnswered());
    }

    @Test
    void untouchedSubjectShowsZeroOfItsQuestions() {
        // Arrange: answers only in subject 1; subject 2 has 5 questions
        List<CountedAnswer> answers = List.of(answer(11, MONDAY, true));

        // Act
        List<SubjectPerformanceResponse> rows = StatisticsService.toSubjectMastery(
                answers,
                List.of(subject(1, "Weapons", 4), subject(2, "Wildlife", 5)));

        // Assert: one row per subject in the bank's order; Wildlife is 0 of 5
        assertEquals(2, rows.size());
        SubjectPerformanceResponse wildlife = rows.get(1);
        assertEquals(2L, wildlife.getSubjectId());
        assertEquals("Wildlife", wildlife.getSubjectName());
        assertEquals(0, wildlife.getMastered());
        assertEquals(5, wildlife.getTotal());
        assertEquals(0, wildlife.getAnswered());
        assertEquals(0, wildlife.getPercentage());
    }

    @Test
    void subjectWithoutQuestionsHasNoPercentage() {
        // Act: a subject with no questions, and nothing to divide by
        SubjectPerformanceResponse row = StatisticsService
                .toSubjectMastery(List.of(), List.of(subject(1, "Weapons", 0)))
                .getFirst();

        // Assert
        assertEquals(0, row.getTotal());
        assertEquals(0, row.getPercentage());
    }

    @Test
    void noAnswersStillGiveOneRowPerSubject() {
        // Act: a brand-new student
        List<SubjectPerformanceResponse> rows = StatisticsService.toSubjectMastery(
                List.of(),
                List.of(subject(1, "Weapons", 4), subject(2, "Wildlife", 5)));

        // Assert: every subject is listed with 0 mastered, so the page can
        // show "not started" instead of hiding it
        assertEquals(2, rows.size());
        assertEquals(0, rows.getFirst().getMastered());
        assertEquals(4, rows.getFirst().getTotal());
    }

    // ----- Progress over time: masteryPerDay -----

    @Test
    void onePointPerDayFromFirstToLastDayOldestFirst() {
        // Act: one whole week
        List<ProgressOverTimeResponse> points = StatisticsService.masteryPerDay(
                List.of(answer(11, MONDAY, true)), 4, MONDAY, MONDAY.plusDays(6));

        // Assert
        assertEquals(7, points.size());
        assertEquals(MONDAY, points.getFirst().date());
        assertEquals(MONDAY.plusDays(6), points.getLast().date());
    }

    @Test
    void rightWrongRightGivesOneZeroOne() {
        // Arrange: one question in the bank, right → wrong → right on three days
        List<CountedAnswer> answers = List.of(
                answer(11, MONDAY, true),
                answer(11, MONDAY.plusDays(1), false),
                answer(11, MONDAY.plusDays(2), true));

        // Act
        List<ProgressOverTimeResponse> points =
                StatisticsService.masteryPerDay(answers, 1, MONDAY, MONDAY.plusDays(2));

        // Assert: the series dips and rises again, not only up
        assertEquals(List.of(1L, 0L, 1L),
                points.stream().map(ProgressOverTimeResponse::mastered).toList());
        assertEquals(List.of(100L, 0L, 100L),
                points.stream().map(ProgressOverTimeResponse::percentage).toList());
    }

    @Test
    void answersBeforeTheWindowSetTheFirstDayButAreNotPractice() {
        // Arrange: two questions mastered the week before the window
        List<CountedAnswer> answers = List.of(
                answer(11, MONDAY.minusDays(7), true),
                answer(12, MONDAY.minusDays(7), true));

        // Act
        ProgressOverTimeResponse first = StatisticsService
                .masteryPerDay(answers, 4, MONDAY, MONDAY.plusDays(1))
                .getFirst();

        // Assert: 2 of 4 carried in, but no practice on the window's first day
        assertEquals(2, first.mastered());
        assertEquals(50, first.percentage());
        assertFalse(first.practised());
    }

    @Test
    void daysWithoutAnswersKeepTheValueAndAreNotPractised() {
        // Act: one answer on Monday, nothing on Tuesday and Wednesday
        List<ProgressOverTimeResponse> points = StatisticsService.masteryPerDay(
                List.of(answer(11, MONDAY, true)), 4, MONDAY, MONDAY.plusDays(2));

        // Assert
        assertTrue(points.get(0).practised());
        assertEquals(1, points.get(2).mastered());
        assertFalse(points.get(1).practised());
        assertFalse(points.get(2).practised());
    }

    @Test
    void noAnswersGiveNoPoints() {
        assertTrue(StatisticsService
                .masteryPerDay(List.of(), 4, MONDAY, MONDAY.plusDays(6))
                .isEmpty());
    }

    @Test
    void emptyBankGivesNoPoints() {
        // Answers exist, but there is nothing to master: no division by zero
        assertTrue(StatisticsService
                .masteryPerDay(List.of(answer(11, MONDAY, true)), 0, MONDAY, MONDAY.plusDays(6))
                .isEmpty());
    }

    // ----- Progress over time: lastDayPerWeek -----

    /**
     * {@code count} daily points from {@link #MONDAY}: day {@code i} has
     * {@code i} mastered (so each day is recognisable) and is practised only
     * if its index is in {@code practisedDays}.
     */
    private static List<ProgressOverTimeResponse> days(int count, int... practisedDays) {
        List<ProgressOverTimeResponse> days = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = i;
            boolean practised = IntStream.of(practisedDays).anyMatch(day -> day == index);
            days.add(new ProgressOverTimeResponse(MONDAY.plusDays(i), i, i, 100, practised));
        }
        return days;
    }

    @Test
    void onePointPerWeekWithItsLastDaysValue() {
        // Act: two whole weeks
        List<ProgressOverTimeResponse> weeks = StatisticsService.lastDayPerWeek(days(14));

        // Assert: each week's Sunday, with that Sunday's value
        assertEquals(2, weeks.size());
        assertEquals(MONDAY.plusDays(6), weeks.get(0).date());
        assertEquals(6, weeks.get(0).mastered());
        assertEquals(MONDAY.plusDays(13), weeks.get(1).date());
        assertEquals(13, weeks.get(1).mastered());
    }

    @Test
    void aWeekIsPractisedIfAnyOfItsDaysWas() {
        // Act: two weeks, practice only on the first week's Wednesday
        List<ProgressOverTimeResponse> weeks = StatisticsService.lastDayPerWeek(days(14, 2));

        // Assert
        assertTrue(weeks.get(0).practised());
        assertFalse(weeks.get(1).practised());
    }

    @Test
    void theUnfinishedWeekEndsOnItsLastDay() {
        // Act: one whole week plus Monday–Wednesday of the next ("today")
        List<ProgressOverTimeResponse> weeks = StatisticsService.lastDayPerWeek(days(10));

        // Assert
        assertEquals(2, weeks.size());
        assertEquals(MONDAY.plusDays(9), weeks.getLast().date());
    }
}
