package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.AdminStatsResponse;
import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.PracticeResultRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
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
    @Mock PracticeResultRepository practiceResultRepository;
    @Mock ExamResultRepository examResultRepository;
    @Mock QuestionRepository questionRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock SubjectStatsRepository subjectStatsRepository;

    private AdminService adminService;
    private User learner;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, practiceResultRepository, examResultRepository,
                questionRepository, subjectRepository, subjectStatsRepository);
        learner = new User();
        learner.setId(1L);
        learner.setFullName("Test Learner");
        learner.setEmail("learner@test.com");
        learner.setRole(Role.STUDENT);
        learner.setCreatedAt(LocalDateTime.now().minusDays(30));
    }

    private PracticeResult practice(Subject subject, long score, LocalDateTime at) {
        PracticeResult r = new PracticeResult();
        r.setSubject(subject);
        r.setScore(score);
        r.setCompletedAt(at);
        r.setUser(learner);
        return r;
    }

    private ExamResult exam(int correct, int total, int score, LocalDateTime at) {
        ExamResult r = new ExamResult();
        r.setCorrectAnswers(correct);
        r.setTotalQuestions(total);
        r.setScore(score);
        r.setCompletedAt(at);
        r.setUser(learner);
        return r;
    }

    /** A student's running totals in one subject. */
    private SubjectStats stats(User user, Subject subject, long answered, long correct) {
        SubjectStats s = new SubjectStats(user, subject);
        s.setAnswered(answered);
        s.setCorrect(correct);
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
        when(practiceResultRepository.findByUser(learner)).thenReturn(List.of());
        when(examResultRepository.findByUserOrderByCompletedAtDesc(learner)).thenReturn(List.of());

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
        when(practiceResultRepository.findByUser(learner)).thenReturn(List.of());
        when(examResultRepository.findByUserOrderByCompletedAtDesc(learner))
                .thenReturn(List.of(
                        exam(62, 70, 89, examTime),
                        exam(55, 70, 79, examTime.minusDays(5))
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
        when(practiceResultRepository.findByUser(learner))
                .thenReturn(List.of(practice(ekologi, 80, practiceTime)));
        when(examResultRepository.findByUserOrderByCompletedAtDesc(learner)).thenReturn(List.of());
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner)))
                .thenReturn(List.of(stats(learner, ekologi, 10, 8)));

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
        when(practiceResultRepository.findByUser(learner))
                .thenReturn(List.of(practice(ekologi, 70, practiceTime)));
        when(examResultRepository.findByUserOrderByCompletedAtDesc(learner))
                .thenReturn(List.of(exam(62, 70, 89, examTime)));
        when(subjectStatsRepository.findAnsweredByUserIn(List.of(learner)))
                .thenReturn(List.of(stats(learner, ekologi, 10, 7)));

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
        when(practiceResultRepository.findByUser(learner)).thenReturn(List.of());
        when(examResultRepository.findByUserOrderByCompletedAtDesc(learner))
                .thenReturn(List.of(exam(59, 70, 84, examTime)));  // 59/70 = 84%

        LearnerProgressResponse lr = adminService.getLearnerProgress().getFirst();

        assertFalse(lr.mockExams().latestPassed());
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
    void noStudentsMeansNoSubjectStatsQuery() {
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of());

        assertTrue(adminService.getLearnerProgress().isEmpty());
        verify(subjectStatsRepository, never()).findAnsweredByUserIn(any());
    }

    @Test
    void statsCountQuestionsSubjectsAndEveryStudent() {
        when(questionRepository.count()).thenReturn(42L);
        when(subjectRepository.count()).thenReturn(5L);
        when(userRepository.countByRole(Role.STUDENT)).thenReturn(17L);

        AdminStatsResponse stats = adminService.getStats();

        assertEquals(42L, stats.questionCount());
        assertEquals(5L, stats.subjectCount());
        assertEquals(17L, stats.studentCount());
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
    }
}