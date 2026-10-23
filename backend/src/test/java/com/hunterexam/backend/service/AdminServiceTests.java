package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.PracticeResultRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTests {

    @Mock UserRepository userRepository;
    @Mock PracticeResultRepository practiceResultRepository;
    @Mock ExamResultRepository examResultRepository;

    private AdminService adminService;
    private User learner;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, practiceResultRepository, examResultRepository);
        learner = new User();
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

        LearnerProgressResponse lr = adminService.getLearnerProgress().getFirst();

        assertEquals(0, lr.mockExams().count());
        assertNull(lr.mockExams().latestScore());
        assertEquals(practiceTime, lr.lastActivityAt());
        assertEquals(1, lr.subjectPerformances().size());
        assertEquals("Ekologi", lr.subjectPerformances().getFirst().getSubjectName());
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
}