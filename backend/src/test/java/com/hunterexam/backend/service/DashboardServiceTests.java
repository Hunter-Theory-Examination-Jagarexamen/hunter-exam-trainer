package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.DashboardResponse;
import com.hunterexam.backend.dto.RecentActivityResponse;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import com.hunterexam.backend.repository.projection.MockExamScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardService}. The repositories are Mockito
 * mocks; the queries themselves are tested in the repository tests.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    private static final String EMAIL = "student@example.test";

    @Mock UserRepository userRepository;
    @Mock AttemptQuestionRepository attemptQuestionRepository;
    @Mock SubjectStatsRepository subjectStatsRepository;

    private DashboardService dashboardService;
    private User user;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(userRepository, attemptQuestionRepository, subjectStatsRepository);
        user = new User();
        user.setId(1L);
        user.setEmail(EMAIL);
    }

    /** A scored 70-question mock exam, {@code daysAgo} days old. */
    private MockExamScore exam(long attemptId, long correct, int daysAgo) {
        return new MockExamScore(attemptId, user.getId(), now.minusDays(daysAgo), 70L, correct);
    }

    @Test
    void cardsCountScoredExamsAndEveryAnsweredQuestion() {
        // Arrange: two scored exams (63/70 = 90 %, 49/70 = 70 %), and 340 answered
        // questions in total (practice + the exams), from SubjectStats
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptQuestionRepository.findScoredMockExams(List.of(user)))
                .thenReturn(List.of(exam(2, 63, 1), exam(1, 49, 3)));
        when(subjectStatsRepository.countAnsweredByUser(user)).thenReturn(340L);

        // Act
        DashboardResponse cards = dashboardService.getDashboard(EMAIL);

        // Assert
        assertEquals(2, cards.getMockExams());
        assertEquals(340, cards.getQuestionsAnswered());
        assertEquals(80, cards.getAverageScore());
        assertEquals(90, cards.getBestScore());
    }

    @Test
    void practiceOnlyStudentSeesAnsweredQuestionsButNoExamFigures() {
        // Arrange: the bug from #180: 200 practice answers used to show as 0
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectStatsRepository.countAnsweredByUser(user)).thenReturn(200L);

        // Act (findScoredMockExams isn't stubbed, so the mock returns an empty list)
        DashboardResponse cards = dashboardService.getDashboard(EMAIL);

        // Assert
        assertEquals(0, cards.getMockExams());
        assertEquals(200, cards.getQuestionsAnswered());
        assertEquals(0, cards.getAverageScore());
        assertEquals(0, cards.getBestScore());
    }

    @Test
    void newStudentSeesZeroEverywhere() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        DashboardResponse cards = dashboardService.getDashboard(EMAIL);

        assertEquals(0, cards.getMockExams());
        assertEquals(0, cards.getQuestionsAnswered());
        assertEquals(0, cards.getAverageScore());
        assertEquals(0, cards.getBestScore());
    }

    @Test
    void recentActivityShowsTheFiveNewestExamsWithTheirScores() {
        // Arrange: 7 scored exams, newest first (attempt ids 7..1), as the query returns them
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        List<MockExamScore> exams = LongStream.rangeClosed(1, 7)
                .map(n -> 8 - n)
                .mapToObj(id -> exam(id, 60, (int) (8 - id)))
                .toList();
        when(attemptQuestionRepository.findScoredMockExams(List.of(user))).thenReturn(exams);

        // Act
        List<RecentActivityResponse> recent = dashboardService.getRecentActivity(EMAIL);

        // Assert: only the 5 newest, in order, with the exam's numbers
        assertEquals(5, recent.size());
        assertEquals(7L, recent.getFirst().getId());
        assertEquals(3L, recent.getLast().getId());
        assertEquals(60, recent.getFirst().getCorrectAnswers());
        assertEquals(70, recent.getFirst().getTotalQuestions());
        assertEquals(86, recent.getFirst().getScore());   // 60 / 70 = 85.7 %
        assertEquals(now.minusDays(1), recent.getFirst().getCompletedAt());
    }

    @Test
    void recentActivityIsEmptyWithoutExams() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertTrue(dashboardService.getRecentActivity(EMAIL).isEmpty());
    }

    @Test
    void unknownUserIsNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> dashboardService.getDashboard(EMAIL));
    }
}
