package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Question mastery as a whole (#193), against the real database: the latest
 * answer per question decides, the same numbers come out of the student's
 * Statistics and the admin's Learner Progress, and the progress series
 * follows the latest answer over time (right → wrong → right dips and rises
 * again instead of only going up).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuestionMasteryStatisticsTests {

    private static final String EMAIL = "mastery-stats@example.test";

    @Autowired private StatisticsService statisticsService;
    @Autowired private AdminService adminService;
    @Autowired private UserRepository users;
    @Autowired private SubjectRepository subjects;
    @Autowired private QuestionRepository questions;
    @Autowired private AttemptRepository attempts;
    @Autowired private AttemptQuestionRepository attemptQuestions;
    @Autowired private PasswordEncoder passwordEncoder;

    private User student;
    private Subject subject;
    private Question first;
    private Question second;
    private Question third;
    private Question neverAnswered;

    /** Three distinct days inside any 4-week chart window. */
    private final LocalDate rightDay = LocalDate.now().minusDays(3);
    private final LocalDate wrongDay = LocalDate.now().minusDays(2);
    private final LocalDate rightAgainDay = LocalDate.now().minusDays(1);

    @BeforeEach
    void setUp() {
        student = new User();
        student.setEmail(EMAIL);
        student.setFullName("Mastery Stats");
        student.setPassword(passwordEncoder.encode("secret123"));
        student.setRole(Role.STUDENT);
        student.setCreatedAt(LocalDateTime.now());
        student.setShareSubjectStatistics(true);
        student.setShareMockExamResults(true);
        student = users.saveAndFlush(student);

        subject = new Subject();
        subject.setName("Mastery-test");
        subject.setDescription("Question mastery statistics");
        subjects.saveAndFlush(subject);

        first = newQuestion("First");
        second = newQuestion("Second");
        third = newQuestion("Third");
        neverAnswered = newQuestion("Never answered");
    }

    private Question newQuestion(String text) {
        Question question = new Question();
        question.setQuestionText(text + " question of Mastery-test");
        question.setOptionA("A");
        question.setOptionB("B");
        question.setOptionC("C");
        question.setOptionD("D");
        question.setCorrectAnswer("A");
        question.setSubject(subject);
        return questions.saveAndFlush(question);
    }

    /** One practice answer of the student to one question, given on {@code day}. */
    private void answer(Question question, boolean correct, LocalDate day) {
        LocalDateTime at = day.atTime(12, 0);
        Attempt round = new Attempt(student, AttemptType.SUBJECT_PRACTICE, at.minusMinutes(10));
        round.setFinishedAt(at);
        round.setScored(false);
        attempts.saveAndFlush(round);

        AttemptQuestion row = new AttemptQuestion(round, question, 0);
        row.recordAnswer(correct ? "A" : "B", correct, at);
        attemptQuestions.saveAndFlush(row);
    }

    private SubjectPerformanceResponse studentRow() {
        return statisticsService.getSubjectPerformance(EMAIL).stream()
                .filter(row -> row.getSubjectId().equals(subject.getId()))
                .findFirst()
                .orElseThrow();
    }

    private SubjectPerformanceResponse adminRow() {
        LearnerProgressResponse progress = adminService.getLearnerProgress(student.getId());
        return progress.subjectPerformances().stream()
                .filter(row -> row.getSubjectId().equals(subject.getId()))
                .findFirst()
                .orElseThrow();
    }

    private ProgressOverTimeResponse pointOn(List<ProgressOverTimeResponse> points, LocalDate day) {
        return points.stream()
                .filter(point -> point.date().equals(day))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void latestAnswerPerQuestionDecidesAndBothViewsShowTheSameNumbers() {
        // Arrange: first is right, then wrong, then right again; second right
        // twice (a repeat never counts twice); third wrong; fourth untouched.
        answer(first, true, rightDay);
        answer(second, true, rightDay);
        answer(third, false, rightDay);
        answer(first, false, wrongDay);
        answer(second, true, wrongDay);
        answer(first, true, rightAgainDay);

        // Act
        SubjectPerformanceResponse student = studentRow();
        SubjectPerformanceResponse admin = adminRow();

        // Assert: 2 of the subject's 4 questions mastered (first and second),
        // 6 counted answers, 50 % — identical for student and course leader.
        assertThat(student.getMasteredQuestions()).isEqualTo(2);
        assertThat(student.getTotalQuestions()).isEqualTo(4);
        assertThat(student.getAnsweredQuestions()).isEqualTo(6);
        assertThat(student.getMasteryPercentage()).isEqualTo(50);

        assertThat(admin.getMasteredQuestions()).isEqualTo(student.getMasteredQuestions());
        assertThat(admin.getTotalQuestions()).isEqualTo(student.getTotalQuestions());
        assertThat(admin.getAnsweredQuestions()).isEqualTo(student.getAnsweredQuestions());
        assertThat(admin.getMasteryPercentage()).isEqualTo(student.getMasteryPercentage());
    }

    @Test
    void theChartDipsWhenALaterAnswerIsWrongAndRisesAgain() {
        // Arrange: one question mastered on day one, lost on day two,
        // mastered again on day three — with the whole bank as denominator.
        answer(first, true, rightDay);

        // Act: state after each day, read from the 4-week series.
        List<ProgressOverTimeResponse> afterFirst = statisticsService.getProgressOverTime(EMAIL, 4);

        answer(first, false, wrongDay);
        List<ProgressOverTimeResponse> afterWrong = statisticsService.getProgressOverTime(EMAIL, 4);

        answer(first, true, rightAgainDay);
        List<ProgressOverTimeResponse> afterRightAgain = statisticsService.getProgressOverTime(EMAIL, 4);

        // Assert: 1 mastered → 0 → 1, as a share of the whole bank (not just
        // this subject), and days without answers hold the previous day's
        // value instead of disappearing.
        long bankSize = questions.count();
        assertThat(pointOn(afterFirst, rightDay).masteredQuestions()).isEqualTo(1);
        assertThat(pointOn(afterFirst, rightDay).masteryPercentage())
                .isEqualTo(Math.round(100.0 / bankSize));
        assertThat(pointOn(afterFirst, wrongDay).masteredQuestions())
                .isEqualTo(pointOn(afterFirst, rightDay).masteredQuestions());

        assertThat(pointOn(afterWrong, wrongDay).masteredQuestions()).isZero();
        assertThat(pointOn(afterWrong, wrongDay).masteryPercentage()).isZero();

        assertThat(pointOn(afterRightAgain, rightAgainDay).masteredQuestions()).isEqualTo(1);
        assertThat(pointOn(afterRightAgain, rightAgainDay).masteryPercentage())
                .isEqualTo(Math.round(100.0 / bankSize));

        // The bank's size is the denominator on every day of the series.
        assertThat(afterRightAgain).allMatch(point -> point.totalQuestions() == bankSize);
        assertThat(afterRightAgain.getLast().date()).isEqualTo(LocalDate.now());
        assertThat(afterRightAgain.getLast().masteredQuestions()).isEqualTo(1);
    }

    @Test
    void studentAndSubjectWithNoAnswersShowZeroMastery() {
        SubjectPerformanceResponse student = studentRow();
        SubjectPerformanceResponse admin = adminRow();

        assertThat(student.getMasteredQuestions()).isZero();
        assertThat(student.getTotalQuestions()).isEqualTo(4);
        assertThat(student.getAnsweredQuestions()).isZero();
        assertThat(student.getMasteryPercentage()).isZero();
        assertThat(admin.getMasteredQuestions()).isZero();
        assertThat(admin.getTotalQuestions()).isEqualTo(4);
        assertThat(admin.getMasteryPercentage()).isZero();
        assertThat(statisticsService.getProgressOverTime(EMAIL, 4)).isEmpty();
    }
}
