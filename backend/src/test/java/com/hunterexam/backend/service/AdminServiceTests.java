package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.AdminStatsResponse;
import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.FeedbackRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTests {

    @Mock UserRepository userRepository;
    @Mock AttemptQuestionRepository attemptQuestionRepository;
    @Mock QuestionRepository questionRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock SubjectStatsRepository subjectStatsRepository;
    @Mock FeedbackRepository feedbackRepository;

    private AdminService adminService;
    private User learner;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, attemptQuestionRepository,
                questionRepository, subjectRepository, subjectStatsRepository, feedbackRepository);
        learner = new User();
        learner.setId(1L);
        learner.setFullName("Test Learner");
        learner.setEmail("learner@test.com");
        learner.setRole(Role.STUDENT);
        learner.setCreatedAt(LocalDateTime.now().minusDays(30));
    }

    /** One scored mock exam of the learner, as the grouped query returns it. */
    private MockExamScore exam(long correct, long total, LocalDateTime at) {
        return new MockExamScore(100L, learner.getId(), at, total, correct);
    }

    /** A student's running totals in one subject. */
    private SubjectStats stats(User user, Subject subject, long answered, long correct) {
        return stats(user, subject, answered, correct, null);
    }

    /** Same, with the time of the student's latest answer in that subject. */
    private SubjectStats stats(User user, Subject subject, long answered, long correct,
                               LocalDateTime lastAnsweredAt) {
        SubjectStats s = new SubjectStats(user, subject);
        s.setAnswered(answered);
        s.setCorrect(correct);
        s.setLastAnsweredAt(lastAnsweredAt);
        return s;
    }

    private Subject subject(long id, String name) {
        Subject s = new Subject();
        s.setId(id);
        s.setName(name);
        return s;
    }

    @Test
    void learnerWithNoResultsShowsEmptyState() {
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(learner));

        List<LearnerProgressResponse> result = adminService.getLearnerProgress();

        assertEquals(1, result.size());
        LearnerProgressResponse lr = result.getFirst();
        assertEquals(0, lr.mockExams().count());
        assertNull(lr.mockExams().latestScore());
        assertNull(lr.mockExams().bestScore());
        assertNull(lr.mockExams().latestPassed());
        assertNull(lr.lastActivityAt());
        assertTrue(lr.subjectPerformances().isEmpty());
    }

    @Test
    void learnerWithOnlyMockExamsShowsExamsAndLastActivity() {
        LocalDateTime examTime = LocalDateTime.now().minusDays(2);
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(learner));
        when(attemptQuestionRepository.findScoredMockExams(List.of(learner)))
                .thenReturn(List.of(
                        exam(62, 70, examTime),
                        exam(55, 70, examTime.minusDays(5))
                ));

        LearnerProgressResponse lr = adminService.getLearnerProgress().getFirst();

        assertEquals(2, lr.mockExams().count());
        assertEquals(89, lr.mockExams().latestScore());
        assertEquals(89, lr.mockExams().bestScore());
        assertEquals(examTime, lr.mockExams().latestAt());
        assertTrue(lr.mockExams().latestPassed());
        assertEquals(examTime, lr.lastActivityAt());
        assertTrue(lr.subjectPerformances().isEmpty());
    }

    @Test
    void learnerWithOnlyPracticeShowsPracticeAndLastActivity() {
        LocalDateTime practiceTime = LocalDateTime.now().minusDays(1);
        Subject ekologi = subject(3L, "Ekologi");
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(learner));
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner)))
                .thenReturn(List.of(stats(learner, ekologi, 10, 8, practiceTime)));

        LearnerProgressResponse lr = adminService.getLearnerProgress().getFirst();

        assertEquals(0, lr.mockExams().count());
        assertNull(lr.mockExams().latestScore());
        assertEquals(practiceTime, lr.lastActivityAt());
        assertEquals(1, lr.subjectPerformances().size());
        assertEquals("Ekologi", lr.subjectPerformances().getFirst().getSubjectName());
        assertEquals(80, lr.subjectPerformances().getFirst().getPercentage());
    }

    @Test
    void learnerWithBothShowsLatestActivity() {
        LocalDateTime practiceTime = LocalDateTime.now().minusDays(5);
        LocalDateTime examTime = LocalDateTime.now().minusDays(1);  // more recent
        Subject ekologi = subject(3L, "Ekologi");
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(learner));
        when(attemptQuestionRepository.findScoredMockExams(List.of(learner)))
                .thenReturn(List.of(exam(62, 70, examTime)));
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner)))
                .thenReturn(List.of(stats(learner, ekologi, 10, 7, practiceTime)));

        LearnerProgressResponse lr = adminService.getLearnerProgress().getFirst();

        assertEquals(1, lr.mockExams().count());
        assertEquals(89, lr.mockExams().latestScore());
        assertEquals(examTime, lr.lastActivityAt());  // should pick the newer exam, not practice
        assertEquals(1, lr.subjectPerformances().size());
    }

    @Test
    void latestExamBelowPassThresholdFails() {
        LocalDateTime examTime = LocalDateTime.now();
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(learner));
        when(attemptQuestionRepository.findScoredMockExams(List.of(learner)))
                .thenReturn(List.of(exam(59, 70, examTime)));  // 59/70 = 84%

        LearnerProgressResponse lr = adminService.getLearnerProgress().getFirst();

        assertFalse(lr.mockExams().latestPassed());
    }

    @Test
    void lastActivityIsTheNewestAnswerAcrossSubjects() {
        // Arrange: answers in two subjects; Vapen was answered more recently
        LocalDateTime older = LocalDateTime.now().minusDays(4);
        LocalDateTime newer = LocalDateTime.now().minusHours(3);
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(learner));
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner))).thenReturn(List.of(
                stats(learner, subject(3L, "Ekologi"), 10, 8, older),
                stats(learner, subject(4L, "Vapen"), 5, 2, newer)));

        // Act + Assert
        assertEquals(newer, adminService.getLearnerProgress().getFirst().lastActivityAt());
    }

    @Test
    void eachLearnerGetsOwnSubjectsFromOneQueryWeakestFirst() {
        // Arrange: two students; all their subject totals come back from one query
        User other = new User();
        other.setId(2L);
        other.setFullName("Other Learner");
        Subject ekologi = subject(3L, "Ekologi");
        Subject vapen = subject(4L, "Vapen");
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(learner, other));
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner, other))).thenReturn(List.of(
                stats(learner, ekologi, 20, 18),   // 90 %
                stats(learner, vapen, 10, 4),      // 40 %
                stats(other, ekologi, 3, 1)));     // 33 %

        // Act: sorted by name, so "Other Learner" comes before "Test Learner"
        List<LearnerProgressResponse> result = adminService.getLearnerProgress();

        // Assert: each student sees only their own rows, weakest subject first
        LearnerProgressResponse otherProgress = result.get(0);
        LearnerProgressResponse learnerProgress = result.get(1);
        assertEquals(1, otherProgress.subjectPerformances().size());
        assertEquals(33, otherProgress.subjectPerformances().getFirst().getPercentage());
        assertEquals(2, learnerProgress.subjectPerformances().size());
        assertEquals("Vapen", learnerProgress.subjectPerformances().get(0).getSubjectName());
        assertEquals(40, learnerProgress.subjectPerformances().get(0).getPercentage());
        assertEquals(90, learnerProgress.subjectPerformances().get(1).getPercentage());
        // One query for all students, not one per student
        verify(subjectStatsRepository).findAnsweredByUserIn(any());
    }

    @Test
    void noStudentsMeansNoSubjectStatsOrExamQuery() {
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of());

        assertTrue(adminService.getLearnerProgress().isEmpty());
        verify(subjectStatsRepository, never()).findAnsweredByUserIn(any());
        verify(attemptQuestionRepository, never()).findScoredMockExams(any());
    }

    @Test
    void eachLearnerGetsOwnMockExamsFromOneQuery() {
        // Arrange: two students; the query returns all scored exams, newest first
        User other = new User();
        other.setId(2L);
        other.setFullName("Other Learner");
        LocalDateTime now = LocalDateTime.now();
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(learner, other));
        when(attemptQuestionRepository.findScoredMockExams(List.of(learner, other))).thenReturn(List.of(
                new MockExamScore(11L, 1L, now.minusDays(1), 70L, 50L),   // learner, latest: 71 %
                new MockExamScore(12L, 2L, now.minusDays(2), 70L, 65L),   // other: 93 %, passed
                new MockExamScore(13L, 1L, now.minusDays(3), 70L, 63L))); // learner, best: 90 %

        // Act: sorted by name, so "Other Learner" comes first
        List<LearnerProgressResponse> result = adminService.getLearnerProgress();

        // Assert
        LearnerProgressResponse otherProgress = result.get(0);
        LearnerProgressResponse learnerProgress = result.get(1);
        assertEquals(1, otherProgress.mockExams().count());
        assertTrue(otherProgress.mockExams().latestPassed());
        assertEquals(2, learnerProgress.mockExams().count());
        assertEquals(71, learnerProgress.mockExams().latestScore());
        assertEquals(90, learnerProgress.mockExams().bestScore());
        assertFalse(learnerProgress.mockExams().latestPassed());   // 50 of 70
        assertEquals(now.minusDays(1), learnerProgress.lastActivityAt());
        verify(attemptQuestionRepository).findScoredMockExams(any());
    }

    @Test
    void statsCountQuestionsSubjectsAndEveryStudent() {
        when(questionRepository.count()).thenReturn(42L);
        when(subjectRepository.count()).thenReturn(5L);
        when(userRepository.countByRole(Role.STUDENT)).thenReturn(17L);
        when(feedbackRepository.countByHandledFalse()).thenReturn(3L);

        AdminStatsResponse stats = adminService.getStats();

        assertEquals(42L, stats.questionCount());
        assertEquals(5L, stats.subjectCount());
        assertEquals(17L, stats.studentCount());
        assertEquals(3L, stats.unhandledFeedbackCount());
        // Students are counted by role alone, so inactive accounts stay included.
        verify(userRepository).countByRole(Role.STUDENT);
    }

    @Test
    void statsAreZeroWhenTheRepositoriesAreEmpty() {
        when(questionRepository.count()).thenReturn(0L);
        when(subjectRepository.count()).thenReturn(0L);
        when(userRepository.countByRole(Role.STUDENT)).thenReturn(0L);

        AdminStatsResponse stats = adminService.getStats();

        assertEquals(0L, stats.questionCount());
        assertEquals(0L, stats.subjectCount());
        assertEquals(0L, stats.studentCount());
        assertEquals(0L, stats.unhandledFeedbackCount());
    }
}