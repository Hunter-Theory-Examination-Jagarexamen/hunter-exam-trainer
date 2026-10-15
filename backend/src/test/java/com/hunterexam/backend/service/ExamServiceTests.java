package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamStartResponse;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
        ReflectionTestUtils.setField(user, "id", 1L);

        // Ownership checks in submitExam and startExam need the user lookup.
        // lenient() so tests that never reach the lookup don't fail strictness checks.
        lenient().when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
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
        givenOpenSessionWith(questions, LocalDateTime.now());
    }

    /** Overloads givenOpenSessionWith: the session started at the given time. */
    private void givenOpenSessionWith(List<Question> questions, LocalDateTime startedAt) {
        ExamSession session = new ExamSession(user, startedAt);
        session.setQuestionIds(questions.stream().map(Question::getId).toList());
        when(examSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(questionRepository.findAllById(questions.stream().map(Question::getId).toList()))
                .thenReturn(questions);
    }

    /** A modifiable list of `count` questions with ids 1..count. */
    private List<Question> questionBank(int count) {
        List<Question> bank = new ArrayList<>();
        for (long id = 1; id <= count; id++) {
            bank.add(question(id));
        }
        return bank;
    }

    private ExamSubmitRequest submission(List<Long> questionIds, Map<String, String> answers) {
        ExamSubmitRequest request = new ExamSubmitRequest();
        request.setSessionId(SESSION_ID);
        request.setQuestionIds(questionIds);
        request.setAnswers(answers);
        return request;
    }

    // ----- Security: server-side question list -----

    @Test
    void clientCannotReduceQuestionCountBySendingOnlyCorrectIds() {
        // Arrange: server issued 4 questions for this session
        givenOpenSessionWith(List.of(question(1), question(2), question(3), question(4)));

        // Client tries to send only question 1 (which it got right)
        ExamSubmitRequest request = submission(List.of(1L),
                Map.of("1", "B"));

        // Act: server ignores the client list, uses all 4 session questions
        ExamResultResponse result = examService.submitExam(request, EMAIL);

        // Assert: total is 4 (not 1), score is 25% (not 100%)
        assertEquals(4, result.getTotalQuestions());
        assertEquals(1, result.getCorrectAnswers());
        assertEquals(3, result.getUnanswered());
        assertEquals(25, result.getScore());
    }

    @Test
    void clientQuestionIdsThatDifferFromSessionAreIgnored() {
        // Arrange: server issued questions 1, 2, 3 for this session
        givenOpenSessionWith(List.of(question(1), question(2), question(3)));

        // Client sends different ids entirely (999) — should be ignored
        ExamSubmitRequest request = submission(List.of(999L),
                Map.of("1", "B", "2", "B", "3", "B"));

        // Act
        ExamResultResponse result = examService.submitExam(request, EMAIL);

        // Assert: server still scored against 1, 2, 3
        assertEquals(3, result.getTotalQuestions());
        assertEquals(3, result.getCorrectAnswers());
        assertEquals(100, result.getScore());
    }

    // ----- Security: ownership -----

    @Test
    void otherUserCannotSubmitSomeonesSession() {
        // Arrange: session belongs to user (id=1)
        ExamSession session = new ExamSession(user, LocalDateTime.now());
        session.setQuestionIds(List.of(1L));
        when(examSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

        // A different user is logged in
        User otherUser = new User();
        otherUser.setEmail("other@example.test");
        ReflectionTestUtils.setField(otherUser, "id", 99L);
        when(userRepository.findByEmail("other@example.test")).thenReturn(Optional.of(otherUser));

        ExamSubmitRequest request = submission(List.of(1L), Map.of("1", "B"));

        // Act + Assert: AccessDeniedException, no scoring, no result saved
        assertThrows(AccessDeniedException.class,
                () -> examService.submitExam(request, "other@example.test"));

        verifyNoInteractions(questionRepository);
        verifyNoInteractions(examResultRepository);
    }

    // ----- Security: session is linked to user at start -----

    @Test
    void startExamLinksSessionToTheLoggedInUserAndStoresQuestionIds() {
        // Arrange
        when(questionRepository.findAll()).thenReturn(questionBank(100));

        ExamSession savedSession = new ExamSession(user, LocalDateTime.now());
        ReflectionTestUtils.setField(savedSession, "id", 42L);
        when(examSessionRepository.save(any(ExamSession.class))).thenReturn(savedSession);

        // Act
        examService.startExam(EMAIL);

        // Assert: the saved session has the user and 70 question ids
        ArgumentCaptor<ExamSession> captor = ArgumentCaptor.forClass(ExamSession.class);
        verify(examSessionRepository).save(captor.capture());
        ExamSession captured = captor.getValue();

        assertSame(user, captured.getUser());
        assertEquals(70, captured.getQuestionIds().size());
        assertEquals(70, captured.getQuestionIds().stream().distinct().count());
    }

    // ----- Scoring -----

    @Test
    void allCorrectAnswersGiveFullScore() {
        givenOpenSessionWith(List.of(question(1), question(2), question(3), question(4)));
        ExamSubmitRequest request = submission(List.of(1L, 2L, 3L, 4L),
                Map.of("1", "B", "2", "B", "3", "B", "4", "B"));

        ExamResultResponse result = examService.submitExam(request, EMAIL);

        assertEquals(4, result.getTotalQuestions());
        assertEquals(4, result.getCorrectAnswers());
        assertEquals(0, result.getIncorrectAnswers());
        assertEquals(0, result.getUnanswered());
        assertEquals(100, result.getScore());
    }

    @Test
    void mixedAnswersAreCountedAsCorrectIncorrectAndUnanswered() {
        givenOpenSessionWith(List.of(question(1), question(2), question(3), question(4)));
        ExamSubmitRequest request = submission(List.of(1L, 2L, 3L, 4L),
                Map.of("1", "B", "2", "A", "4", "B"));

        ExamResultResponse result = examService.submitExam(request, EMAIL);

        assertEquals(4, result.getTotalQuestions());
        assertEquals(2, result.getCorrectAnswers());
        assertEquals(1, result.getIncorrectAnswers());
        assertEquals(1, result.getUnanswered());
        assertEquals(50, result.getScore());
    }

    @Test
    void answerThatIsNotALetterAToDCountsAsIncorrect() {
        givenOpenSessionWith(List.of(question(1), question(2)));
        ExamSubmitRequest request = submission(List.of(1L, 2L),
                Map.of("1", "B", "2", "X"));

        ExamResultResponse result = examService.submitExam(request, EMAIL);

        assertEquals(1, result.getCorrectAnswers());
        assertEquals(1, result.getIncorrectAnswers());
        assertEquals(0, result.getUnanswered());
    }

    @Test
    void scoreIsRoundedToNearestWholePercent() {
        givenOpenSessionWith(List.of(question(1), question(2), question(3)));
        ExamSubmitRequest request = submission(List.of(1L, 2L, 3L),
                Map.of("1", "B", "2", "B", "3", "A"));

        ExamResultResponse result = examService.submitExam(request, EMAIL);

        assertEquals(67, result.getScore());
    }

    @Test
    void savedResultHasTheScoreAndBelongsToTheUser() {
        givenOpenSessionWith(List.of(question(1), question(2), question(3), question(4)));
        ExamSubmitRequest request = submission(List.of(1L, 2L, 3L, 4L),
                Map.of("1", "B", "2", "A", "4", "B"));

        examService.submitExam(request, EMAIL);

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
        ExamSubmitRequest request = submission(List.of(1L), Map.of("1", "B"));
        request.setSessionId(null);

        assertThrows(IllegalArgumentException.class,
                () -> examService.submitExam(request, EMAIL));

        verifyNoInteractions(examSessionRepository);
        verifyNoInteractions(examResultRepository);
    }

    @Test
    void submitWithUnknownSessionIdIsRejected() {
        when(examSessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());
        ExamSubmitRequest request = submission(List.of(1L), Map.of("1", "B"));

        assertThrows(IllegalArgumentException.class,
                () -> examService.submitExam(request, EMAIL));

        verifyNoInteractions(questionRepository);
        verifyNoInteractions(examResultRepository);
    }

    @Test
    void sessionThatIsAlreadySubmittedCannotBeSubmittedAgain() {
        ExamSession session = new ExamSession(user, LocalDateTime.now());
        session.setCompleted(true);
        when(examSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        ExamSubmitRequest request = submission(List.of(1L), Map.of("1", "B"));

        assertThrows(IllegalArgumentException.class,
                () -> examService.submitExam(request, EMAIL));

        verifyNoInteractions(questionRepository);
        verifyNoInteractions(examResultRepository);
    }

    @Test
    void submitAfterTheTimeLimitIsRejectedAndClosesTheSession() {
        ExamSession session = new ExamSession(user, LocalDateTime.now().minusMinutes(61));
        when(examSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        ExamSubmitRequest request = submission(List.of(1L), Map.of("1", "B"));

        assertThrows(IllegalArgumentException.class,
                () -> examService.submitExam(request, EMAIL));

        assertTrue(session.isCompleted(), "a late session should be closed");
        verify(examSessionRepository).save(session);
        verifyNoInteractions(questionRepository);
        verifyNoInteractions(examResultRepository);
    }

    @Test
    void submitJustBeforeTheTimeLimitIsAccepted() {
        givenOpenSessionWith(List.of(question(1)), LocalDateTime.now().minusMinutes(59));
        ExamSubmitRequest request = submission(List.of(1L), Map.of("1", "B"));

        ExamResultResponse result = examService.submitExam(request, EMAIL);

        assertEquals(1, result.getCorrectAnswers());
        verify(examResultRepository).save(any(ExamResult.class));
    }

    // ----- Starting an exam -----

    @Test
    void startExamWithFewerThan70QuestionsIsRejected() {
        when(questionRepository.findAll()).thenReturn(questionBank(69));

        assertThrows(RuntimeException.class, () -> examService.startExam(EMAIL));

        verifyNoInteractions(examSessionRepository);
    }

    @Test
    void startExamGives70DifferentQuestionsAndTheSessionId() {
        when(questionRepository.findAll()).thenReturn(questionBank(100));

        ExamSession savedSession = new ExamSession(user, LocalDateTime.now());
        ReflectionTestUtils.setField(savedSession, "id", 42L);
        when(examSessionRepository.save(any(ExamSession.class))).thenReturn(savedSession);

        ExamStartResponse response = examService.startExam(EMAIL);

        assertEquals(42L, response.getSessionId());
        assertEquals(70, response.getQuestions().size());
        long differentIds = response.getQuestions().stream()
                .map(q -> q.getId())
                .distinct()
                .count();
        assertEquals(70, differentIds, "no question should appear twice");
    }
}