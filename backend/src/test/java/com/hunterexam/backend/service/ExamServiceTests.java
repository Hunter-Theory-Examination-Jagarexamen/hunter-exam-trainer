package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamStartResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.ExamTimeLimitExceededException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExamService}.
 * <p>
 * The repositories are Mockito mocks, so no database is used.
 */
@ExtendWith(MockitoExtension.class)
class ExamServiceTests {

    private static final Long ATTEMPT_ID = 1L;
    private static final String EMAIL = "student@example.test";

    @Mock QuestionRepository questionRepository;
    @Mock ExamResultRepository examResultRepository;
    @Mock UserRepository userRepository;
    @Mock AttemptRepository attemptRepository;
    @Mock AttemptQuestionRepository attemptQuestionRepository;
    @Mock SubjectStatsRepository subjectStatsRepository;

    private ExamService examService;
    private User user;
    private Subject subject;

    @BeforeEach
    void setUp() {
        examService = new ExamService(questionRepository, examResultRepository,
            userRepository, attemptRepository, attemptQuestionRepository, subjectStatsRepository);
        user = new User();
        user.setEmail(EMAIL);
        ReflectionTestUtils.setField(user, "id", 1L);

        // Ownership checks in submitExam and startExam need the user lookup.
        // lenient() so tests that never reach the lookup don't fail strictness checks.
        lenient().when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        subject = new Subject();
        subject.setId(7L);
        // Submit updates the student's totals row; 1 = "one row updated" (the row exists).
        lenient().when(subjectStatsRepository.addAnswer(any(), any(), anyLong(), any())).thenReturn(1);
    }

    /** A question whose correct answer is option B ("Two"). */
    private Question question(long id) {
        Question question = new Question();
        question.setId(id);
        question.setOptionA("One");
        question.setOptionB("Two");
        question.setOptionC("Three");
        question.setOptionD("Four");
        question.setCorrectAnswer("B");
        question.setSubject(subject);
        return question;
    }

    /** Fakes a running exam with the given answers already saved on its rows. */
    private Attempt givenOpenExamWith(List<Question> questions, Map<Long, String> answers) {
        return givenOpenExamWith(questions, answers, LocalDateTime.now());
    }

    /** Overload: the exam started at the given time. */
    private Attempt givenOpenExamWith(
        List<Question> questions,
        Map<Long, String> answers,
        LocalDateTime startedAt
    ) {
        Attempt exam = givenAttempt(AttemptType.MOCK_EXAM, startedAt);
        List<AttemptQuestion> rows = new ArrayList<>();
        for (int position = 0; position < questions.size(); position++) {
            Question question = questions.get(position);
            AttemptQuestion row = new AttemptQuestion(exam, question, position);
            String answer = answers.get(question.getId());
            if (answer != null) {
                row.recordAnswer(answer, answer.equals(question.getCorrectAnswer()), startedAt);
            }
            rows.add(row);
        }
        when(attemptQuestionRepository.findByAttemptIdOrderByPosition(ATTEMPT_ID)).thenReturn(rows);
        return exam;
    }

    /** A modifiable list of `count` questions with ids 1..count. */
    private List<Question> questionBank(int count) {
        List<Question> bank = new ArrayList<>();
        for (long id = 1; id <= count; id++) {
            bank.add(question(id));
        }
        return bank;
    }

    private ExamSubmitRequest submission() {
        ExamSubmitRequest request = new ExamSubmitRequest();
        request.setAttemptId(ATTEMPT_ID);
        return request;
    }

    // ----- saveAnswer -----

    /** Fakes a stored attempt of the given type, owned by the user, started now. */
    private Attempt givenAttempt(AttemptType type) {
        return givenAttempt(type, LocalDateTime.now());
    }

    /** Overload: the attempt started at the given time. */
    private Attempt givenAttempt(AttemptType type, LocalDateTime startedAt) {
        Attempt attempt = new Attempt(user, type, startedAt);
        ReflectionTestUtils.setField(attempt, "id", ATTEMPT_ID);
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(attempt));
        return attempt;
    }

    /** Fakes the attempt's row for the given question (correct answer B). */
    private AttemptQuestion givenRow(Attempt attempt, long questionId) {
        AttemptQuestion row = new AttemptQuestion(attempt, question(questionId), 0);
        when(attemptQuestionRepository.findByAttemptIdAndQuestionId(ATTEMPT_ID, questionId))
            .thenReturn(Optional.of(row));
        return row;
    }

    @Test
    void saveAnswerStoresTheAnswerOnTheQuestionsRow() {
        AttemptQuestion row = givenRow(givenAttempt(AttemptType.MOCK_EXAM), 1L);

        examService.saveAnswer(ATTEMPT_ID, 1L, "B", EMAIL);

        assertTrue(row.isAnswered());
        assertEquals("B", row.getChosenOption());
        assertTrue(row.getCorrect());
        verify(attemptQuestionRepository).save(row);
    }

    @Test
    void saveAnswerCanChangeAnEarlierAnswer() {
        AttemptQuestion row = givenRow(givenAttempt(AttemptType.MOCK_EXAM), 1L);

        examService.saveAnswer(ATTEMPT_ID, 1L, "B", EMAIL);
        examService.saveAnswer(ATTEMPT_ID, 1L, "C", EMAIL);

        assertEquals("C", row.getChosenOption());
        assertFalse(row.getCorrect());
    }

    @Test
    void saveAnswerRejectsAPracticeRound() {
        // Practice answers may only be given once; this endpoint must not change them.
        givenAttempt(AttemptType.SUBJECT_PRACTICE);

        assertThrows(NotFoundException.class,
            () -> examService.saveAnswer(ATTEMPT_ID, 1L, "B", EMAIL));
    }

    @Test
    void saveAnswerRejectsUnknownExam() {
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> examService.saveAnswer(ATTEMPT_ID, 1L, "B", EMAIL));
    }

    // ----- Security: ownership -----

    @Test
    void saveAnswerRejectsOtherUser() {
        givenAttempt(AttemptType.MOCK_EXAM);

        // A different user is logged in
        User otherUser = new User();
        otherUser.setEmail("other@example.test");
        ReflectionTestUtils.setField(otherUser, "id", 99L);
        when(userRepository.findByEmail("other@example.test")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class,
            () -> examService.saveAnswer(ATTEMPT_ID, 1L, "B", "other@example.test"));
    }

    // ----- saveAnswer: only a running exam, only its own questions -----

    @Test
    void saveAnswerRejectsSubmittedExam() {
        Attempt exam = givenAttempt(AttemptType.MOCK_EXAM);
        ReflectionTestUtils.setField(exam, "finishedAt", LocalDateTime.now());

        assertThrows(ConflictException.class,
            () -> examService.saveAnswer(ATTEMPT_ID, 1L, "B", EMAIL));
    }

    @Test
    void saveAnswerRejectsQuestionNotInTheExam() {
        givenAttempt(AttemptType.MOCK_EXAM);
        when(attemptQuestionRepository.findByAttemptIdAndQuestionId(ATTEMPT_ID, 999L))
            .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> examService.saveAnswer(ATTEMPT_ID, 999L, "B", EMAIL));
    }

    // ----- submitExam: uses server-saved answers -----

    @Test
    void submitUsesServerSavedAnswers() {
        givenOpenExamWith(
            List.of(question(1), question(2), question(3), question(4)),
            Map.of(1L, "B", 2L, "B", 3L, "B", 4L, "B")
        );

        ExamResultResponse result = examService.submitExam(submission(), EMAIL);

        assertEquals(4, result.getTotalQuestions());
        assertEquals(4, result.getCorrectAnswers());
        assertEquals(100, result.getScore());
    }

    @Test
    void submitIgnoresClientProvidedAnswers() {
        // Server has correct answers; client sends wrong ones (ignored)
        givenOpenExamWith(
            List.of(question(1), question(2)),
            Map.of(1L, "B", 2L, "B")
        );

        ExamSubmitRequest request = submission();
        request.setAnswers(Map.of("1", "A", "2", "A"));  // wrong, ignored

        ExamResultResponse result = examService.submitExam(request, EMAIL);

        assertEquals(2, result.getCorrectAnswers());
        assertEquals(100, result.getScore());
    }

    // ----- Scoring -----

    @Test
    void allCorrectAnswersGiveFullScore() {
        givenOpenExamWith(
            List.of(question(1), question(2), question(3), question(4)),
            Map.of(1L, "B", 2L, "B", 3L, "B", 4L, "B")
        );

        ExamResultResponse result = examService.submitExam(submission(), EMAIL);

        assertEquals(4, result.getTotalQuestions());
        assertEquals(4, result.getCorrectAnswers());
        assertEquals(0, result.getIncorrectAnswers());
        assertEquals(0, result.getUnanswered());
        assertEquals(100, result.getScore());
    }

    @Test
    void mixedAnswersAreCountedAsCorrectIncorrectAndUnanswered() {
        // Q1 right, Q2 wrong, Q3 skipped, Q4 right
        givenOpenExamWith(
            List.of(question(1), question(2), question(3), question(4)),
            Map.of(1L, "B", 2L, "A", 4L, "B")
        );

        ExamResultResponse result = examService.submitExam(submission(), EMAIL);

        assertEquals(4, result.getTotalQuestions());
        assertEquals(2, result.getCorrectAnswers());
        assertEquals(1, result.getIncorrectAnswers());
        assertEquals(1, result.getUnanswered());
        assertEquals(50, result.getScore());
    }

    @Test
    void scoreIsRoundedToNearestWholePercent() {
        // 2 of 3 right = 66.67%
        givenOpenExamWith(
            List.of(question(1), question(2), question(3)),
            Map.of(1L, "B", 2L, "B", 3L, "A")
        );

        ExamResultResponse result = examService.submitExam(submission(), EMAIL);

        assertEquals(67, result.getScore());
    }

    @Test
    void savedResultHasTheScoreAndBelongsToTheUser() {
        givenOpenExamWith(
            List.of(question(1), question(2), question(3), question(4)),
            Map.of(1L, "B", 2L, "A", 4L, "B")
        );

        examService.submitExam(submission(), EMAIL);

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

    @Test
    void scoresAllIssuedQuestionsNotOnlyTheAnsweredOnes() {
        // Server issued 3 questions, only 1 answer saved
        givenOpenExamWith(
            List.of(question(1), question(2), question(3)),
            Map.of(1L, "B")
        );

        ExamResultResponse result = examService.submitExam(submission(), EMAIL);

        assertEquals(3, result.getTotalQuestions());
        assertEquals(1, result.getCorrectAnswers());
        assertEquals(2, result.getUnanswered());
    }

    // ----- submitExam: closes the exam and adds to the subject totals -----

    @Test
    void submitClosesTheExam() {
        Attempt exam = givenOpenExamWith(List.of(question(1)), Map.of(1L, "B"));

        examService.submitExam(submission(), EMAIL);

        assertTrue(exam.isFinished());
        verify(attemptRepository).save(exam);
    }

    @Test
    void submitAddsEachAnsweredQuestionToTheSubjectTotals() {
        // Q1 right, Q2 wrong, Q3 skipped
        givenOpenExamWith(
            List.of(question(1), question(2), question(3)),
            Map.of(1L, "B", 2L, "A")
        );

        examService.submitExam(submission(), EMAIL);

        // One update per answered question: +1 correct for Q1, +0 for Q2, nothing for Q3.
        verify(subjectStatsRepository).addAnswer(eq(user), eq(subject), eq(1L), any());
        verify(subjectStatsRepository).addAnswer(eq(user), eq(subject), eq(0L), any());
        verify(subjectStatsRepository, times(2)).addAnswer(any(), any(), anyLong(), any());
    }

    @Test
    void submittedExamCannotBeAnsweredAfterwards() {
        Attempt exam = givenOpenExamWith(List.of(question(1)), Map.of(1L, "B"));
        examService.submitExam(submission(), EMAIL);
        assertTrue(exam.isFinished());

        assertThrows(ConflictException.class,
            () -> examService.saveAnswer(ATTEMPT_ID, 1L, "A", EMAIL));
    }

    @Test
    void practiceRoundCannotBeSubmittedAsAnExam() {
        givenAttempt(AttemptType.SUBJECT_PRACTICE);

        assertThrows(NotFoundException.class,
            () -> examService.submitExam(submission(), EMAIL));

        verifyNoInteractions(examResultRepository, subjectStatsRepository);
    }

    // ----- Security: ownership -----

    @Test
    void otherUserCannotSubmitSomeonesExam() {
        givenAttempt(AttemptType.MOCK_EXAM);

        User otherUser = new User();
        otherUser.setEmail("other@example.test");
        ReflectionTestUtils.setField(otherUser, "id", 99L);
        when(userRepository.findByEmail("other@example.test")).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class,
            () -> examService.submitExam(submission(), "other@example.test"));

        verifyNoInteractions(attemptQuestionRepository, examResultRepository, subjectStatsRepository);
    }

    @Test
    void startExamCreatesMockExamAttemptWith70QuestionsInOrder() {
        when(questionRepository.findAll()).thenReturn(questionBank(100));
        givenAttemptIsSavedWithId(42L);

        ExamStartResponse response = examService.startExam(EMAIL);

        ArgumentCaptor<Attempt> attempt = ArgumentCaptor.forClass(Attempt.class);
        verify(attemptRepository).save(attempt.capture());
        assertSame(user, attempt.getValue().getUser());
        assertEquals(AttemptType.MOCK_EXAM, attempt.getValue().getType());
        assertFalse(attempt.getValue().isFinished());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AttemptQuestion>> rows = ArgumentCaptor.forClass(List.class);
        verify(attemptQuestionRepository).saveAll(rows.capture());
        assertEquals(70, rows.getValue().size());
        for (int i = 0; i < 70; i++) {
            AttemptQuestion row = rows.getValue().get(i);
            assertSame(attempt.getValue(), row.getAttempt());
            assertEquals(i, row.getPosition());
            assertFalse(row.isAnswered());
            // The page shows the questions in the same order as the rows.
            assertEquals(response.getQuestions().get(i).getId(), row.getQuestion().getId());
        }
    }

    @Test
    void startExamCreatesMissingSubjectTotalsRows() {
        when(questionRepository.findAll()).thenReturn(questionBank(100));
        givenAttemptIsSavedWithId(42L);
        when(subjectStatsRepository.existsByUserAndSubject(user, subject)).thenReturn(false);

        examService.startExam(EMAIL);

        // All test questions share one subject: one row, created once.
        verify(subjectStatsRepository, times(1)).save(any(SubjectStats.class));
    }

    @Test
    void startExamKeepsExistingSubjectTotalsRows() {
        when(questionRepository.findAll()).thenReturn(questionBank(100));
        givenAttemptIsSavedWithId(42L);
        when(subjectStatsRepository.existsByUserAndSubject(user, subject)).thenReturn(true);

        examService.startExam(EMAIL);

        verify(subjectStatsRepository, never()).save(any());
    }

    /** Fakes saving the attempt: the database would give it an id. */
    private void givenAttemptIsSavedWithId(long id) {
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(call -> {
            Attempt saved = call.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", id);
            return saved;
        });
    }

    // ----- Submit rules and time limit -----

    @Test
    void submitWithoutAttemptIdIsRejected() {
        ExamSubmitRequest request = submission();
        request.setAttemptId(null);

        assertThrows(IllegalArgumentException.class,
            () -> examService.submitExam(request, EMAIL));

        verifyNoInteractions(attemptRepository, examResultRepository);
    }

    @Test
    void submitWithUnknownAttemptIdIsRejected() {
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> examService.submitExam(submission(), EMAIL));

        verifyNoInteractions(attemptQuestionRepository, examResultRepository);
    }

    @Test
    void examThatIsAlreadySubmittedCannotBeSubmittedAgain() {
        Attempt exam = givenAttempt(AttemptType.MOCK_EXAM);
        exam.setFinishedAt(LocalDateTime.now());

        // 409 Conflict (before #174 it was 400).
        assertThrows(ConflictException.class,
            () -> examService.submitExam(submission(), EMAIL));

        verifyNoInteractions(attemptQuestionRepository, examResultRepository, subjectStatsRepository);
    }

    @Test
    void submitMoreThanGracePeriodLateIsRejectedAndClosesTheExam() {
        // 63 minutes elapsed = 60 limit + 2 grace + 1 over
        Attempt exam = givenAttempt(AttemptType.MOCK_EXAM, LocalDateTime.now().minusMinutes(63));

        // Still a 400 (it extends IllegalArgumentException).
        assertThrows(ExamTimeLimitExceededException.class,
            () -> examService.submitExam(submission(), EMAIL));

        assertTrue(exam.isFinished(), "a late exam should be closed");
        verify(attemptRepository).save(exam);
        verifyNoInteractions(attemptQuestionRepository, examResultRepository, subjectStatsRepository);
    }

    @Test
    void submitWithinGracePeriodIsAccepted() {
        // 61 minutes elapsed = 60 limit + 2 grace → still accepted
        givenOpenExamWith(
            List.of(question(1)),
            Map.of(1L, "B"),
            LocalDateTime.now().minusMinutes(61)
        );

        ExamResultResponse result = examService.submitExam(submission(), EMAIL);

        assertEquals(1, result.getCorrectAnswers());
        verify(examResultRepository).save(any(ExamResult.class));
    }

    @Test
    void submitJustBeforeTheTimeLimitIsAccepted() {
        givenOpenExamWith(
            List.of(question(1)),
            Map.of(1L, "B"),
            LocalDateTime.now().minusMinutes(59)
        );

        ExamResultResponse result = examService.submitExam(submission(), EMAIL);

        assertEquals(1, result.getCorrectAnswers());
        verify(examResultRepository).save(any(ExamResult.class));
    }

    // ----- Starting an exam -----

    @Test
    void startExamWithFewerThan70QuestionsIsRejected() {
        when(questionRepository.findAll()).thenReturn(questionBank(69));

        assertThrows(ConflictException.class, () -> examService.startExam(EMAIL));

        verifyNoInteractions(attemptRepository, attemptQuestionRepository);
    }

    @Test
    void startExamGives70DifferentQuestionsAndTheAttemptId() {
        when(questionRepository.findAll()).thenReturn(questionBank(100));
        givenAttemptIsSavedWithId(42L);

        ExamStartResponse response = examService.startExam(EMAIL);

        assertEquals(42L, response.getAttemptId());
        assertEquals(70, response.getQuestions().size());
        long differentIds = response.getQuestions().stream()
            .map(q -> q.getId())
            .distinct()
            .count();
        assertEquals(70, differentIds, "no question should appear twice");
    }
}
