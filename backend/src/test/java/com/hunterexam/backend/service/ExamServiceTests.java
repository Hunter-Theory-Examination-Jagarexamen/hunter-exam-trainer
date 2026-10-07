package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.ExamSession;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.ExamSessionRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for exam scoring in {@link ExamService#submitExam}.
 * The repositories are Mockito mocks, so no database is used.
 */
@ExtendWith(MockitoExtension.class)
class ExamServiceTests {

    private static final Long SESSION_ID = 1L;
    private static final String EMAIL = "student@example.test";

    @Mock QuestionRepository questionRepository;
    @Mock ExamResultRepository examResultRepository;
    @Mock UserRepository userRepository;
    @Mock ExamSessionRepository examSessionRepository;

    private ExamService examService;
    private User user;

    @BeforeEach
    void setUp() {
        examService = new ExamService(questionRepository, examResultRepository,
                userRepository, examSessionRepository);
        user = new User();
        user.setEmail(EMAIL);
    }

    /** A question whose correct answer is option B ("Two"). */
    private Question question(long id) {
        Question question = new Question();
        question.setId(id);
        question.setOptionA("One");
        question.setOptionB("Two");
        question.setOptionC("Three");
        question.setOptionD("Four");
        question.setCorrectAnswer("Two");
        return question;
    }

    /** Fakes an open session that started now, plus the questions and the user. */
    private void givenOpenSessionWith(List<Question> questions) {
        ExamSession session = new ExamSession(user, LocalDateTime.now());
        when(examSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(questionRepository.findAllById(questions.stream().map(Question::getId).toList()))
                .thenReturn(questions);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private ExamSubmitRequest submission(List<Long> questionIds, Map<String, String> answers) {
        ExamSubmitRequest request = new ExamSubmitRequest();
        request.setSessionId(SESSION_ID);
        request.setQuestionIds(questionIds);
        request.setAnswers(answers);
        return request;
    }

    // ----- Scoring -----

    @Test
    void allCorrectAnswersGiveFullScore() {
        // Arrange
        givenOpenSessionWith(List.of(question(1), question(2), question(3), question(4)));
        ExamSubmitRequest request = submission(List.of(1L, 2L, 3L, 4L),
                Map.of("1", "B", "2", "B", "3", "B", "4", "B"));

        // Act
        ExamResultResponse result = examService.submitExam(request, EMAIL);

        // Assert
        assertEquals(4, result.getTotalQuestions());
        assertEquals(4, result.getCorrectAnswers());
        assertEquals(0, result.getIncorrectAnswers());
        assertEquals(0, result.getUnanswered());
        assertEquals(100, result.getScore());
    }

    @Test
    void mixedAnswersAreCountedAsCorrectIncorrectAndUnanswered() {
        // Arrange: Q1 right, Q2 wrong, Q3 skipped, Q4 right
        givenOpenSessionWith(List.of(question(1), question(2), question(3), question(4)));
        ExamSubmitRequest request = submission(List.of(1L, 2L, 3L, 4L),
                Map.of("1", "B", "2", "A", "4", "B"));

        // Act
        ExamResultResponse result = examService.submitExam(request, EMAIL);

        // Assert
        assertEquals(4, result.getTotalQuestions());
        assertEquals(2, result.getCorrectAnswers());
        assertEquals(1, result.getIncorrectAnswers());
        assertEquals(1, result.getUnanswered());
        assertEquals(50, result.getScore());
    }

    @Test
    void answerThatIsNotALetterAToDCountsAsIncorrect() {
        // Arrange: Q1 right, Q2 has an invalid letter
        givenOpenSessionWith(List.of(question(1), question(2)));
        ExamSubmitRequest request = submission(List.of(1L, 2L),
                Map.of("1", "B", "2", "X"));

        // Act
        ExamResultResponse result = examService.submitExam(request, EMAIL);

        // Assert
        assertEquals(1, result.getCorrectAnswers());
        assertEquals(1, result.getIncorrectAnswers());
        assertEquals(0, result.getUnanswered());
    }

    @Test
    void scoreIsRoundedToNearestWholePercent() {
        // Arrange: 2 of 3 right = 66.67 %
        givenOpenSessionWith(List.of(question(1), question(2), question(3)));
        ExamSubmitRequest request = submission(List.of(1L, 2L, 3L),
                Map.of("1", "B", "2", "B", "3", "A"));

        // Act
        ExamResultResponse result = examService.submitExam(request, EMAIL);

        // Assert
        assertEquals(67, result.getScore());
    }

    /*
     * Why this test is needed even though the tests above already check the score:
     *
     * submitExam builds TWO separate objects with the same numbers:
     *   1. ExamResultResponse - returned to the browser and shown right after the exam.
     *   2. ExamResult         - saved to the database. The statistics and dashboard
     *                           pages read these saved results later.
     * The tests above only look at (1). If (2) got a wrong number, or no user, the
     * student would see the right score after the exam, but the statistics page
     * would be wrong or the result would not show up for them at all.
     *
     * The service doesn't return the ExamResult, so we can't check it directly.
     * Instead we use an ArgumentCaptor: it "catches" the object the service passes
     * to examResultRepository.save(...), so we can inspect what would have been
     * stored in the database.
     */
    @Test
    void savedResultHasTheScoreAndBelongsToTheUser() {
        // Arrange: same answers as the mixed test (2 right, 1 wrong, 1 skipped)
        givenOpenSessionWith(List.of(question(1), question(2), question(3), question(4)));
        ExamSubmitRequest request = submission(List.of(1L, 2L, 3L, 4L),
                Map.of("1", "B", "2", "A", "4", "B"));

        // Act
        examService.submitExam(request, EMAIL);

        // Assert: catch the ExamResult that the service passed to save(...)
        ArgumentCaptor<ExamResult> saved = ArgumentCaptor.forClass(ExamResult.class);
        verify(examResultRepository).save(saved.capture());
        ExamResult examResult = saved.getValue();

        assertSame(user, examResult.getUser());
        assertEquals(4, examResult.getTotalQuestions());
        assertEquals(2, examResult.getCorrectAnswers());
        assertEquals(1, examResult.getIncorrectAnswers());
        assertEquals(1, examResult.getUnanswered());
        assertEquals(50, examResult.getScore());
        assertNotNull(examResult.getCompletedAt());
    }

    // ----- Session and time-limit rules -----

    @Test
    void submitWithoutSessionIdIsRejected() {
        // Arrange: a submission that is missing its session id
        ExamSubmitRequest request = submission(List.of(1L), Map.of("1", "B"));
        request.setSessionId(null);

        // Act + Assert: the lambda () -> ... is run by assertThrows, which
        // passes only if it throws an IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> examService.submitExam(request, EMAIL));

        // The service must stop at the first check, before looking up any session.
        // Without this line the test would still pass if the null check was removed,
        // because "session not found" is also an IllegalArgumentException.
        verifyNoInteractions(examSessionRepository);

        // A rejected exam must never be saved as a result
        verifyNoInteractions(examResultRepository);
    }

    @Test
    void submitWithUnknownSessionIdIsRejected() {
        // Arrange: the repository finds no session with this id
        // (Optional.empty() is how a repository says "nothing found")
        when(examSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
        ExamSubmitRequest request = submission(List.of(1L), Map.of("1", "B"));

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> examService.submitExam(request, EMAIL));

        // The service must stop at the session lookup and never start scoring
        verifyNoInteractions(questionRepository);
        verifyNoInteractions(examResultRepository);
    }

    @Test
    void sessionThatIsAlreadySubmittedCannotBeSubmittedAgain() {
        // Arrange: a session that has already been submitted once
        ExamSession session = new ExamSession(user, LocalDateTime.now());
        session.setCompleted(true);
        when(examSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        ExamSubmitRequest request = submission(List.of(1L), Map.of("1", "B"));

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> examService.submitExam(request, EMAIL));

        // No second scoring and no second saved result
        verifyNoInteractions(questionRepository);
        verifyNoInteractions(examResultRepository);
    }
}
