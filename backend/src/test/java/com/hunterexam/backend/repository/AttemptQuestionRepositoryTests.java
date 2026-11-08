package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.projection.AnswerOutcome;
import com.hunterexam.backend.repository.projection.MockExamScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the JPQL queries against a real (H2) database:
 * {@code findScoredMockExams} (only scored mock exams count, each exam's score
 * counted from its rows) and {@code findCountedAnswersSince} (the answers the
 * progress chart counts).
 */
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class AttemptQuestionRepositoryTests {

    @Autowired AttemptQuestionRepository attemptQuestions;
    @Autowired AttemptRepository attempts;
    @Autowired QuestionRepository questions;
    @Autowired SubjectRepository subjects;
    @Autowired UserRepository users;

    private final LocalDateTime now = LocalDateTime.now();
    private User student;
    private List<Question> bank;

    @BeforeEach
    void setUp() {
        student = saveUser("student@example.test");
        Subject subject = new Subject();
        subject.setName("Weapons-test");
        subject.setDescription("Weapons-test");
        subjects.saveAndFlush(subject);
        bank = List.of(saveQuestion(subject, 1), saveQuestion(subject, 2),
                saveQuestion(subject, 3), saveQuestion(subject, 4));
    }

    private User saveUser(String email) {
        User user = new User();
        user.setFullName(email);
        user.setEmail(email);
        user.setPassword("irrelevant");
        user.setRole(Role.STUDENT);
        user.setCreatedAt(now);
        return users.saveAndFlush(user);
    }

    private Question saveQuestion(Subject subject, int n) {
        Question question = new Question();
        question.setQuestionText("Question " + n);
        question.setOptionA("A");
        question.setOptionB("B");
        question.setOptionC("C");
        question.setOptionD("D");
        question.setCorrectAnswer("A");
        question.setSubject(subject);
        return questions.saveAndFlush(question);
    }

    /**
     * Saves a 4-question round. {@code answers} holds one entry per question:
     * {@code true} = right, {@code false} = wrong, {@code null} = not answered.
     */
    private Attempt saveRound(User user, AttemptType type, LocalDateTime finishedAt,
                              boolean scored, Boolean... answers) {
        Attempt attempt = new Attempt(user, type, now.minusHours(2));
        attempt.setFinishedAt(finishedAt);
        attempt.setScored(scored);
        attempts.saveAndFlush(attempt);
        for (int i = 0; i < bank.size(); i++) {
            AttemptQuestion row = new AttemptQuestion(attempt, bank.get(i), i + 1);
            if (answers[i] != null) {
                row.recordAnswer(answers[i] ? "A" : "B", answers[i], now.minusHours(1));
            }
            attemptQuestions.saveAndFlush(row);
        }
        return attempt;
    }

    @Test
    void countsEachScoredMockExamFromItsRowsNewestFirst() {
        // Arrange: two scored exams (3 of 4 and 1 of 4, one question unanswered)
        Attempt older = saveRound(student, AttemptType.MOCK_EXAM, now.minusDays(2), true,
                true, true, true, false);
        Attempt newer = saveRound(student, AttemptType.MOCK_EXAM, now.minusDays(1), true,
                true, false, false, null);

        // Act
        List<MockExamScore> scores = attemptQuestions.findScoredMockExams(List.of(student));

        // Assert: one row per exam, newest first, unanswered counts as not correct
        assertEquals(2, scores.size());
        assertEquals(newer.getId(), scores.get(0).attemptId());
        assertEquals(4L, scores.get(0).total());
        assertEquals(1L, scores.get(0).correct());
        assertEquals(25, scores.get(0).score());
        assertEquals(older.getId(), scores.get(1).attemptId());
        assertEquals(3L, scores.get(1).correct());
        assertEquals(student.getId(), scores.get(1).userId());
    }

    @Test
    void leavesOutOpenLateAndPracticeRoundsAndOtherStudents() {
        // Arrange: only the first round should count
        User other = saveUser("other@example.test");
        Attempt scored = saveRound(student, AttemptType.MOCK_EXAM, now, true,
                true, true, true, true);
        saveRound(student, AttemptType.MOCK_EXAM, null, false,              // still open
                true, true, null, null);
        saveRound(student, AttemptType.MOCK_EXAM, now, false,               // late submit: closed, not scored
                true, true, true, true);
        saveRound(student, AttemptType.SUBJECT_PRACTICE, now, false,        // practice round
                true, true, true, true);
        saveRound(other, AttemptType.MOCK_EXAM, now, true,                  // another student
                true, true, true, true);

        // Act
        List<MockExamScore> scores = attemptQuestions.findScoredMockExams(List.of(student));

        // Assert
        assertEquals(1, scores.size());
        assertEquals(scored.getId(), scores.getFirst().attemptId());
    }

    @Test
    void countedAnswersArePracticeAndScoredExamsOnly() {
        // Arrange: saveRound gives every answer the time now - 1 hour
        User other = saveUser("other@example.test");
        saveRound(student, AttemptType.SUBJECT_PRACTICE, null, false,       // practice, still open: counts
                true, false, null, null);
        saveRound(student, AttemptType.MOCK_EXAM, now, true,                // scored exam: counts
                true, true, true, null);
        saveRound(student, AttemptType.MOCK_EXAM, null, false,              // open exam: doesn't
                true, true, true, true);
        saveRound(student, AttemptType.MOCK_EXAM, now, false,               // late exam: doesn't
                true, true, true, true);
        saveRound(other, AttemptType.SUBJECT_PRACTICE, now, false,          // another student: doesn't
                true, true, true, true);

        // Act
        List<AnswerOutcome> answers = attemptQuestions.findCountedAnswersSince(student, now.minusDays(1));

        // Assert: 2 practice answers + 3 exam answers; unanswered rows left out
        assertEquals(5, answers.size());
        assertEquals(4, answers.stream().filter(AnswerOutcome::isCorrect).count());
    }

    @Test
    void countedAnswersLeaveOutAnswersBeforeSince() {
        // Arrange: answers at now - 1 hour
        saveRound(student, AttemptType.SUBJECT_PRACTICE, now, false,
                true, true, true, true);

        // Act + Assert: "since" after the answers gives nothing; before gives all 4
        assertTrue(attemptQuestions.findCountedAnswersSince(student, now.minusMinutes(30)).isEmpty());
        assertFalse(attemptQuestions.findCountedAnswersSince(student, now.minusHours(2)).isEmpty());
    }

    @Test
    void abandonedExamsAreNeitherResultsNorCountedAnswers() {
        // Arrange: an abandoned exam (#186) is closed but never scored, even
        // though its answers are saved on its rows.
        saveRound(student, AttemptType.MOCK_EXAM, now, false,
                true, true, false, true);

        // Act
        List<MockExamScore> scores = attemptQuestions.findScoredMockExams(List.of(student));
        List<AnswerOutcome> answers = attemptQuestions.findCountedAnswersSince(student, now.minusDays(1));

        // Assert: no result, and its answers don't count toward statistics
        assertTrue(scores.isEmpty());
        assertTrue(answers.isEmpty());
    }
}
