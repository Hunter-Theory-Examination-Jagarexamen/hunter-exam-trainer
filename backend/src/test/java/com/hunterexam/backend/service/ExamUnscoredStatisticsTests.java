package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamStartResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.dto.ExamSubmitResponse;
import com.hunterexam.backend.dto.LearnerProgressResponse;
import com.hunterexam.backend.dto.ProgressOverTimeResponse;
import com.hunterexam.backend.dto.SubjectPerformanceResponse;
import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.ExamTimeLimitExceededException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Statistics when a mock exam ends without a result (#187), against the real
 * database: the answers given within the time limit count exactly once — no
 * matter how often, or through which path (abandon #186, late submit, the
 * next exam's cleanup), the exam is ended — while the exam never becomes a
 * scored result. The time window is start + 60 minutes + 2 minutes grace,
 * the same bound normal submission uses.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExamUnscoredStatisticsTests {

    private static final String EMAIL = "unscored-stats@example.test";

    @Autowired private ExamService examService;
    @Autowired private StatisticsService statisticsService;
    @Autowired private AdminService adminService;
    @Autowired private UserRepository users;
    @Autowired private SubjectRepository subjects;
    @Autowired private QuestionRepository questions;
    @Autowired private AttemptRepository attempts;
    @Autowired private AttemptQuestionRepository attemptQuestions;
    @Autowired private SubjectStatsRepository subjectStats;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;

    private User student;
    private Subject subject;
    private int questionNumber;

    @BeforeEach
    void setUp() {
        student = newUser(EMAIL);
        subject = new Subject();
        subject.setName("Window-test");
        subject.setDescription("Window-test");
        subjects.saveAndFlush(subject);

        // startExam needs a full bank; DataInitializer seeds one on startup,
        // but seed it here too so this test never depends on that order.
        while (questions.count() < 70) {
            newQuestion();
        }
    }

    private User newUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFullName("Test " + email);
        user.setPassword(passwordEncoder.encode("secret123"));
        user.setRole(Role.STUDENT);
        user.setCreatedAt(LocalDateTime.now());
        // This class checks how results are counted, not whether they are
        // shared, so the student shares both categories (#125).
        user.setShareSubjectStatistics(true);
        user.setShareMockExamResults(true);
        return users.saveAndFlush(user);
    }

    private Question newQuestion() {
        Question question = new Question();
        question.setQuestionText("Question " + (++questionNumber));
        question.setOptionA("A");
        question.setOptionB("B");
        question.setOptionC("C");
        question.setOptionD("D");
        question.setCorrectAnswer("A");
        question.setSubject(subject);
        return questions.saveAndFlush(question);
    }

    /** One exam row: the option given (null = unanswered) and when. */
    private record GivenAnswer(String chosenOption, LocalDateTime answeredAt) {}

    private static GivenAnswer correctAt(LocalDateTime answeredAt) {
        return new GivenAnswer("A", answeredAt);   // the questions' correct answer
    }

    private static GivenAnswer wrongAt(LocalDateTime answeredAt) {
        return new GivenAnswer("B", answeredAt);
    }

    private static GivenAnswer unanswered() {
        return new GivenAnswer(null, null);
    }

    /**
     * Saves a mock exam for the student with one row per given answer, and
     * the per-subject totals row the real flow creates at start.
     */
    private Attempt openExam(LocalDateTime startedAt, GivenAnswer... given) {
        Attempt exam = attempts.saveAndFlush(new Attempt(student, AttemptType.MOCK_EXAM, startedAt));
        int position = 0;
        for (GivenAnswer answer : given) {
            AttemptQuestion row = new AttemptQuestion(exam, newQuestion(), position++);
            if (answer.answeredAt() != null) {
                row.recordAnswer(answer.chosenOption(),
                        "A".equals(answer.chosenOption()), answer.answeredAt());
            }
            attemptQuestions.saveAndFlush(row);
        }
        if (!subjectStats.existsByUserAndSubject(student, subject)) {
            subjectStats.saveAndFlush(new SubjectStats(student, subject));
        }
        return exam;
    }

    private ExamSubmitRequest submission(Attempt exam) {
        ExamSubmitRequest request = new ExamSubmitRequest();
        request.setAttemptId(exam.getId());
        return request;
    }

    /**
     * The student's totals in the subject used by these tests, read as a
     * later request would see them: the totals are updated with a bulk
     * UPDATE that skips the persistence context, so flush the pending
     * entity writes and clear the session first — otherwise a managed
     * {@link SubjectStats} still holds its pre-update state.
     */
    private Totals totals() {
        entityManager.flush();
        entityManager.clear();
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT s.answered, s.correct FROM SubjectStats s"
                                + " WHERE s.user.id = :userId AND s.subject.id = :subjectId",
                        Object[].class)
                .setParameter("userId", student.getId())
                .setParameter("subjectId", subject.getId())
                .getResultList();
        assertThat(rows).hasSize(1);
        return new Totals((Long) rows.getFirst()[0], (Long) rows.getFirst()[1]);
    }

    private record Totals(long answered, long correct) {}

    // ----- The required behaviour: counted once, even when ended twice -----

    @Test
    void lateExamEndedTwiceCountsItsEligibleAnswersExactlyOnce() {
        // Started 70 minutes ago, so the window (start + 62 min) ended 8 minutes ago.
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(70);
        Attempt exam = openExam(startedAt,
                correctAt(startedAt.plusMinutes(10)),         // in the window: counts
                wrongAt(LocalDateTime.now().minusMinutes(5))  // after it: doesn't
        );

        examService.abandonExam(exam.getId(), EMAIL);
        // A retry of the same ending is rejected (409), never counted again.
        assertThatThrownBy(() -> examService.abandonExam(exam.getId(), EMAIL))
                .isInstanceOf(ConflictException.class);

        // Exactly one counted answer, the correct one.
        Totals totals = totals();
        assertThat(totals.answered()).isEqualTo(1);
        assertThat(totals.correct()).isEqualTo(1);

        // The exam ended without a result: closed, unscored, no pass/fail.
        Attempt reloaded = attempts.findById(exam.getId()).orElseThrow();
        assertThat(reloaded.getFinishedAt()).isNotNull();
        assertThat(reloaded.isScored()).isFalse();
        assertThat(attemptQuestions.findScoredMockExams(List.of(student))).isEmpty();
    }

    @Test
    void lateSubmitCountsTheAnswersOnceAndALaterAbandonIsRejected() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(70);
        Attempt exam = openExam(startedAt,
                correctAt(startedAt.plusMinutes(10)),
                correctAt(startedAt.plusMinutes(20)),
                wrongAt(startedAt.plusMinutes(65))            // after the window
        );

        // Submitting late (400) ends the exam without a result but counts
        // the in-window answers through the same ending as abandonment.
        assertThatThrownBy(() -> examService.submitExam(submission(exam), EMAIL))
                .isInstanceOf(ExamTimeLimitExceededException.class);
        assertThatThrownBy(() -> examService.abandonExam(exam.getId(), EMAIL))
                .isInstanceOf(ConflictException.class);

        Totals totals = totals();
        assertThat(totals.answered()).isEqualTo(2);
        assertThat(totals.correct()).isEqualTo(2);
        assertThat(attempts.findById(exam.getId()).orElseThrow().isScored()).isFalse();
        assertThat(attemptQuestions.findScoredMockExams(List.of(student))).isEmpty();
    }

    // ----- #186: abandonment and automatic cleanup -----

    @Test
    void anAbandonedExamContributesItsValidAnswers() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
        Attempt exam = openExam(startedAt,
                correctAt(startedAt.plusMinutes(5)),
                wrongAt(startedAt.plusMinutes(10)),
                unanswered()
        );

        examService.abandonExam(exam.getId(), EMAIL);

        Totals totals = totals();
        assertThat(totals.answered()).isEqualTo(2);
        assertThat(totals.correct()).isEqualTo(1);

        // Visible in the student's per-subject Statistics: 1 of the subject's
        // questions mastered (the right one, the wrong one is not).
        List<SubjectPerformanceResponse> rows = statisticsService.getSubjectPerformance(EMAIL);
        SubjectPerformanceResponse windowRow = rows.stream()
                .filter(row -> row.getSubjectId().equals(subject.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(windowRow.getMasteredQuestions()).isEqualTo(1);
        assertThat(windowRow.getAnsweredQuestions()).isEqualTo(2);
        assertThat(windowRow.getMasteryPercentage())
                .isEqualTo(Math.round(100.0 / windowRow.getTotalQuestions()));
    }

    @Test
    void startingANewExamCountsThePreviousExamsValidAnswersOnce() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(20);
        Attempt earlier = openExam(startedAt,
                correctAt(startedAt.plusMinutes(5)),
                wrongAt(LocalDateTime.now())
        );

        ExamStartResponse started = examService.startExam(EMAIL);

        assertThat(started.getAttemptId()).isNotEqualTo(earlier.getId());
        Totals totals = totals();
        assertThat(totals.answered()).isEqualTo(2);
        assertThat(totals.correct()).isEqualTo(1);
        assertThat(attempts.findById(earlier.getId()).orElseThrow().isScored()).isFalse();

        // Ending the already-closed exam afterwards (e.g. its tab finally
        // abandons) doesn't count anything a second time.
        assertThatThrownBy(() -> examService.abandonExam(earlier.getId(), EMAIL))
                .isInstanceOf(ConflictException.class);
        Totals afterRetry = totals();
        assertThat(afterRetry.answered()).isEqualTo(2);
    }

    // ----- The answers show up everywhere statistics are read -----

    @Test
    void unscoredExamCountsInAdminLearnerProgressButNeverAsAResult() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
        Attempt exam = openExam(startedAt,
                correctAt(startedAt.plusMinutes(5)),
                wrongAt(startedAt.plusMinutes(10))
        );
        examService.abandonExam(exam.getId(), EMAIL);

        // Also refreshes the session, so the reads below see the totals.
        Totals totals = totals();
        assertThat(totals.answered()).isEqualTo(2);
        assertThat(totals.correct()).isEqualTo(1);

        LearnerProgressResponse progress = adminService.getLearnerProgress(student.getId());

        // Per-subject performance: 1 of the subject's questions mastered, like
        // any other counted answer — same numbers as the student's own page.
        assertThat(progress.subjectPerformances()).isNotEmpty();
        SubjectPerformanceResponse windowRow = progress.subjectPerformances().stream()
                .filter(row -> row.getSubjectId().equals(subject.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(windowRow.getMasteredQuestions()).isEqualTo(1);
        assertThat(windowRow.getAnsweredQuestions()).isEqualTo(2);
        assertThat(windowRow.getMasteryPercentage())
                .isEqualTo(Math.round(100.0 / windowRow.getTotalQuestions()));
        // ...but the exam itself is not one of the scored mock exams.
        assertThat(progress.mockExams().count()).isZero();
        assertThat(progress.mockExams().latestScore()).isNull();
        assertThat(progress.mockExams().latestPassed()).isNull();
        assertThat(progress.lastActivityAt()).isNotNull();
    }

    @Test
    void progressOverTimeCountsTheEligibleAnswersOfAnUnscoredExam() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(70);
        Attempt exam = openExam(startedAt,
                correctAt(startedAt.plusMinutes(10)),
                wrongAt(LocalDateTime.now().minusMinutes(5))   // after the window
        );
        examService.abandonExam(exam.getId(), EMAIL);

        Totals totals = totals();
        assertThat(totals.answered()).isEqualTo(1);
        assertThat(totals.correct()).isEqualTo(1);

        List<ProgressOverTimeResponse> points = statisticsService.getProgressOverTime(EMAIL, 4);

        // One point per day from the Monday of that week to today.
        LocalDate from = LocalDate.now()
                .minusWeeks(4)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        assertThat(points)
                .hasSize((int) from.datesUntil(LocalDate.now().plusDays(1)).count());

        // The only eligible answer was given today: every earlier day has
        // nothing mastered yet, and today shows the one mastered question out
        // of the whole bank.
        assertThat(points.subList(0, points.size() - 1))
                .allMatch(point -> point.masteredQuestions() == 0);
        ProgressOverTimeResponse today = points.getLast();
        assertThat(today.masteredQuestions()).isEqualTo(1);
        assertThat(today.totalQuestions()).isEqualTo(questions.count());
        assertThat(today.masteryPercentage())
                .isEqualTo(Math.round(100.0 / today.totalQuestions()));
    }

    // ----- Existing scored behaviour is unchanged -----

    @Test
    void anInTimeSubmitStillScoresAndCountsEveryAnswer() {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(10);
        Attempt exam = openExam(startedAt,
                correctAt(startedAt.plusMinutes(5)),
                wrongAt(startedAt.plusMinutes(6)),
                unanswered()
        );

        ExamSubmitResponse result = examService.submitExam(submission(exam), EMAIL);

        assertThat(result.getTotalQuestions()).isEqualTo(3);
        assertThat(result.getCorrectAnswers()).isEqualTo(1);
        assertThat(result.getIncorrectAnswers()).isEqualTo(1);
        assertThat(result.getUnanswered()).isEqualTo(1);
        assertThat(result.getScore()).isEqualTo(33);

        Totals totals = totals();
        assertThat(totals.answered()).isEqualTo(2);
        assertThat(totals.correct()).isEqualTo(1);
        assertThat(attempts.findById(exam.getId()).orElseThrow().isScored()).isTrue();
        assertThat(attemptQuestions.findScoredMockExams(List.of(student))).hasSize(1);
    }
}
