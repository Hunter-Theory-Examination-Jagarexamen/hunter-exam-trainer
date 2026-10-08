package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.PracticeResultRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the "performance by subject" numbers in {@link StatisticsService}.
 * The repositories are Mockito mocks, so no database is used.
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTests {

    private static final String EMAIL = "student@example.test";

    @Mock PracticeResultRepository practiceResultRepository;
    @Mock UserRepository userRepository;

    private StatisticsService statisticsService;
    private User user;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService(practiceResultRepository, userRepository);
        user = new User();
        user.setEmail(EMAIL);
    }

    private Subject subject(long id, String name) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setName(name);
        return subject;
    }

    /** One finished practice round in a subject, with its score in percent. */
    private PracticeResult practiceResult(Subject subject, long score) {
        PracticeResult result = new PracticeResult();
        result.setSubject(subject);
        result.setScore(score);
        return result;
    }

    /** Finds one subject's row in the result; the service gives no fixed order. */
    private SubjectPerformanceResponse rowFor(List<SubjectPerformanceResponse> rows, long subjectId) {
        return rows.stream()
                .filter(row -> row.getSubjectId() == subjectId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no row for subject " + subjectId));
    }

    // ----- Performance by subject -----

    @Test
    void averageScoreIsCalculatedPerSubject() {
        // Arrange: two rounds in "Weapons" (80 % and 90 %), one in "Wildlife" (50 %)
        Subject weapons = subject(1, "Weapons");
        Subject wildlife = subject(2, "Wildlife");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(practiceResultRepository.findByUser(user)).thenReturn(List.of(
                practiceResult(weapons, 80),
                practiceResult(weapons, 90),
                practiceResult(wildlife, 50)));

        // Act
        List<SubjectPerformanceResponse> rows = statisticsService.getSubjectPerformance(EMAIL);

        // Assert: one row per practised subject, with the average of its rounds
        assertEquals(2, rows.size());
        assertEquals("Weapons", rowFor(rows, 1).getSubjectName());
        assertEquals(85, rowFor(rows, 1).getPercentage());
        assertEquals(50, rowFor(rows, 2).getPercentage());
    }
}
