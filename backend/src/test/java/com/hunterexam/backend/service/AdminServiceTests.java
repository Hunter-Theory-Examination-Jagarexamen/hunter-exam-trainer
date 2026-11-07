package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.AdminStatsResponse;
import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.dto.LearnerSummaryResponse;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.NotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private User user(long id, String fullName, Role role) {
        User u = new User();
        u.setId(id);
        u.setFullName(fullName);
        u.setRole(role);
        return u;
    }

    private Subject subject(long id, String name) {
        Subject s = new Subject();
        s.setId(id);
        s.setName(name);
        return s;
    }

    @Test
    void learnerWithNoResultsShowsEmptyState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(learner));

        LearnerProgressResponse lr = adminService.getLearnerProgress(1L);
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(attemptQuestionRepository.findScoredMockExams(List.of(learner)))
                .thenReturn(List.of(
                        exam(62, 70, examTime),
                        exam(55, 70, examTime.minusDays(5))
                ));

        LearnerProgressResponse lr = adminService.getLearnerProgress(1L);

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
        when(userRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner)))
                .thenReturn(List.of(stats(learner, ekologi, 10, 8, practiceTime)));

        LearnerProgressResponse lr = adminService.getLearnerProgress(1L);

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
        when(userRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(attemptQuestionRepository.findScoredMockExams(List.of(learner)))
                .thenReturn(List.of(exam(62, 70, examTime)));
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner)))
                .thenReturn(List.of(stats(learner, ekologi, 10, 7, practiceTime)));

        LearnerProgressResponse lr = adminService.getLearnerProgress(1L);

        assertEquals(1, lr.mockExams().count());
        assertEquals(89, lr.mockExams().latestScore());
        assertEquals(examTime, lr.lastActivityAt());  // should pick the newer exam, not practice
        assertEquals(1, lr.subjectPerformances().size());
    }

    @Test
    void latestExamBelowPassThresholdFails() {
        LocalDateTime examTime = LocalDateTime.now();
        when(userRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(attemptQuestionRepository.findScoredMockExams(List.of(learner)))
                .thenReturn(List.of(exam(59, 70, examTime)));  // 59/70 = 84%

        LearnerProgressResponse lr = adminService.getLearnerProgress(1L);

        assertFalse(lr.mockExams().latestPassed());
    }

    @Test
    void lastActivityIsTheNewestAnswerAcrossSubjects() {
        // Arrange: answers in two subjects; Vapen was answered more recently
        LocalDateTime older = LocalDateTime.now().minusDays(4);
        LocalDateTime newer = LocalDateTime.now().minusHours(3);
        when(userRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner))).thenReturn(List.of(
                stats(learner, subject(3L, "Ekologi"), 10, 8, older),
                stats(learner, subject(4L, "Vapen"), 5, 2, newer)));

        // Act + Assert
        assertEquals(newer, adminService.getLearnerProgress(1L).lastActivityAt());
    }

    @Test
    void subjectsAreSortedWeakestFirst() {
        Subject ekologi = subject(3L, "Ekologi");
        Subject vapen = subject(4L, "Vapen");
        when(userRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner))).thenReturn(List.of(
                stats(learner, ekologi, 20, 18),   // 90 %
                stats(learner, vapen, 10, 4)));    // 40 %

        LearnerProgressResponse lr = adminService.getLearnerProgress(1L);

        assertEquals("Vapen", lr.subjectPerformances().get(0).getSubjectName());
        assertEquals(40, lr.subjectPerformances().get(0).getPercentage());
        assertEquals(90, lr.subjectPerformances().get(1).getPercentage());
    }

    @Test
    void latestAndBestMockExamCanDiffer() {
        // The query returns exams newest first
        LocalDateTime now = LocalDateTime.now();
        when(userRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(attemptQuestionRepository.findScoredMockExams(List.of(learner))).thenReturn(List.of(
                exam(50, 70, now.minusDays(1)),    // latest: 71 %
                exam(63, 70, now.minusDays(3))));  // best: 90 %

        LearnerProgressResponse lr = adminService.getLearnerProgress(1L);

        assertEquals(2, lr.mockExams().count());
        assertEquals(71, lr.mockExams().latestScore());
        assertEquals(90, lr.mockExams().bestScore());
        assertFalse(lr.mockExams().latestPassed());   // 50 of 70
    }

    @Test
    void unknownUserIdThrowsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> adminService.getLearnerProgress(99L));
    }

    @Test
    void learnersListShowsAdminsFirstThenStudentsByName() {
        User admin = user(5L, "Zara Admin", Role.ADMIN);
        User bertil = user(2L, "bertil Student", Role.STUDENT);   // lower case: sorting ignores case
        when(userRepository.findAll()).thenReturn(List.of(learner, admin, bertil));

        List<LearnerSummaryResponse> result = adminService.getLearners();

        assertEquals(List.of(
                new LearnerSummaryResponse(5L, "Zara Admin", Role.ADMIN),
                new LearnerSummaryResponse(2L, "bertil Student", Role.STUDENT),
                new LearnerSummaryResponse(1L, "Test Learner", Role.STUDENT)
        ), result);
    }

    @Test
    void learnersListIsEmptyWithNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertTrue(adminService.getLearners().isEmpty());
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