package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.PracticeAnswerResponse;
import com.hunterexam.backend.dto.PracticeFinishResponse;
import com.hunterexam.backend.dto.PracticeStartResponse;
import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.PracticeResult;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.PracticeResultRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PracticeAttemptService}: start, answer and finish.
 * <p>
 * The repositories are Mockito mocks, so no database is used.
 */
@ExtendWith(MockitoExtension.class)
class PracticeAttemptServiceTests {

    private static final String EMAIL = "student@example.test";
    private static final Long ATTEMPT_ID = 10L;

    @Mock AttemptRepository attemptRepository;
    @Mock AttemptQuestionRepository attemptQuestionRepository;
    @Mock PracticeResultRepository practiceResultRepository;
    @Mock QuestionRepository questionRepository;
    @Mock SubjectRepository subjectRepository;
    @Mock SubjectStatsRepository subjectStatsRepository;
    @Mock UserRepository userRepository;

    private PracticeAttemptService service;
    private User user;
    private Subject subject;

    @BeforeEach
    void setUp() {
        service = new PracticeAttemptService(attemptRepository, attemptQuestionRepository,
                practiceResultRepository, questionRepository, subjectRepository,
                subjectStatsRepository, userRepository);

        user = user(1L);
        subject = new Subject();
        subject.setId(5L);
        subject.setName("Viltvård");
    }

    // ---------- helpers ----------

    private static User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setEmail(EMAIL);
        return u;
    }

    // A question whose correct answer is option B ("Two"), stored as text like today.
    private Question question(Long id) {
        Question q = new Question();
        q.setId(id);
        q.setQuestionText("Question " + id);
        q.setOptionA("One");
        q.setOptionB("Two");
        q.setOptionC("Three");
        q.setOptionD("Four");
        q.setCorrectAnswer("Two");
        q.setExplanation("Because two.");
        q.setSubject(subject);
        return q;
    }

    private Attempt runningAttempt(User owner) {
        Attempt attempt = new Attempt(owner, AttemptType.SUBJECT_PRACTICE, LocalDateTime.now());
        attempt.setId(ATTEMPT_ID);
        return attempt;
    }

    // Sets up: the user exists, the round exists, and question 100 is in it.
    private AttemptQuestion givenRoundWithQuestion(Attempt attempt) {
        AttemptQuestion row = new AttemptQuestion(attempt, question(100L), 0);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(attempt));
        when(attemptQuestionRepository.findByAttemptIdAndQuestionId(ATTEMPT_ID, 100L))
                .thenReturn(Optional.of(row));
        return row;
    }

    // ---------- start ----------

    @Test
    void start_createsRoundWithOneRowPerQuestionInOrder_andHidesCorrectAnswers() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findById(5L)).thenReturn(Optional.of(subject));
        when(questionRepository.findBySubjectId(5L)).thenReturn(List.of(question(100L), question(101L)));
        // save() returns the attempt with an id, like the database would.
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(call -> {
            Attempt saved = call.getArgument(0);
            saved.setId(ATTEMPT_ID);
            return saved;
        });
        when(subjectStatsRepository.existsByUserAndSubject(user, subject)).thenReturn(true);

        PracticeStartResponse response = service.start(5L, EMAIL);

        assertEquals(ATTEMPT_ID, response.getAttemptId());
        assertEquals(2, response.getQuestions().size());
        // ExamQuestionResponse has no correctAnswer field at all; the ids prove the order.
        assertEquals(100L, response.getQuestions().get(0).getId());
        assertEquals(101L, response.getQuestions().get(1).getId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AttemptQuestion>> rows = ArgumentCaptor.forClass(List.class);
        verify(attemptQuestionRepository).saveAll(rows.capture());
        assertEquals(2, rows.getValue().size());
        assertEquals(0, rows.getValue().get(0).getPosition());
        assertEquals(1, rows.getValue().get(1).getPosition());
        assertFalse(rows.getValue().get(0).isAnswered());
    }

    @Test
    void start_createsSubjectStatsRow_whenStudentHasNone() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findById(5L)).thenReturn(Optional.of(subject));
        when(questionRepository.findBySubjectId(5L)).thenReturn(List.of(question(100L)));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(call -> call.getArgument(0));
        when(subjectStatsRepository.existsByUserAndSubject(user, subject)).thenReturn(false);

        service.start(5L, EMAIL);

        ArgumentCaptor<SubjectStats> stats = ArgumentCaptor.forClass(SubjectStats.class);
        verify(subjectStatsRepository).save(stats.capture());
        assertEquals(user, stats.getValue().getUser());
        assertEquals(subject, stats.getValue().getSubject());
        assertEquals(0, stats.getValue().getAnswered());
        assertEquals(0, stats.getValue().getCorrect());
    }

    @Test
    void start_throwsNotFound_whenSubjectDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.start(99L, EMAIL));
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void start_throwsConflict_whenSubjectHasNoQuestions() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findById(5L)).thenReturn(Optional.of(subject));
        when(questionRepository.findBySubjectId(5L)).thenReturn(List.of());

        assertThrows(ConflictException.class, () -> service.start(5L, EMAIL));
        verify(attemptRepository, never()).save(any());
        verify(attemptQuestionRepository, never()).saveAll(anyList());
    }

    // ---------- answer ----------

    @Test
    void answer_correct_savesAnswerAndAddsOneCorrectToStats() {
        AttemptQuestion row = givenRoundWithQuestion(runningAttempt(user));
        when(subjectStatsRepository.addAnswer(eq(user), eq(subject), eq(1L), any())).thenReturn(1);

        PracticeAnswerResponse response = service.answer(ATTEMPT_ID, 100L, "B", EMAIL);

        assertTrue(response.isCorrect());
        assertEquals("B", response.getCorrectOption());
        assertEquals("Because two.", response.getExplanation());
        assertTrue(row.isAnswered());
        assertEquals("B", row.getChosenOption());
        assertTrue(row.getCorrect());
        verify(attemptQuestionRepository).save(row);
    }

    @Test
    void answer_wrong_savesAnswerAndAddsZeroCorrectToStats() {
        AttemptQuestion row = givenRoundWithQuestion(runningAttempt(user));
        when(subjectStatsRepository.addAnswer(eq(user), eq(subject), eq(0L), any())).thenReturn(1);

        PracticeAnswerResponse response = service.answer(ATTEMPT_ID, 100L, "C", EMAIL);

        assertFalse(response.isCorrect());
        assertEquals("B", response.getCorrectOption());
        assertFalse(row.getCorrect());
        verify(subjectStatsRepository).addAnswer(eq(user), eq(subject), eq(0L), any());
    }

    @Test
    void answer_throwsAccessDenied_whenRoundBelongsToSomeoneElse() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(runningAttempt(user(2L))));

        assertThrows(AccessDeniedException.class, () -> service.answer(ATTEMPT_ID, 100L, "B", EMAIL));
        verify(attemptQuestionRepository, never()).save(any());
    }

    @Test
    void answer_throwsConflict_whenRoundIsFinished() {
        Attempt finished = runningAttempt(user);
        finished.setFinishedAt(LocalDateTime.now());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(finished));

        assertThrows(ConflictException.class, () -> service.answer(ATTEMPT_ID, 100L, "B", EMAIL));
    }

    @Test
    void answer_throwsBadRequest_whenQuestionIsNotInRound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(runningAttempt(user)));
        when(attemptQuestionRepository.findByAttemptIdAndQuestionId(ATTEMPT_ID, 999L))
                .thenReturn(Optional.empty());

        // IllegalArgumentException becomes 400 Bad Request in GlobalExceptionHandler.
        assertThrows(IllegalArgumentException.class, () -> service.answer(ATTEMPT_ID, 999L, "B", EMAIL));
    }

    @Test
    void answer_throwsConflict_whenQuestionAlreadyAnswered_andKeepsFirstAnswer() {
        AttemptQuestion row = givenRoundWithQuestion(runningAttempt(user));
        row.recordAnswer("A", false, LocalDateTime.now());

        assertThrows(ConflictException.class, () -> service.answer(ATTEMPT_ID, 100L, "B", EMAIL));
        assertEquals("A", row.getChosenOption());
        verify(subjectStatsRepository, never()).addAnswer(any(), any(), anyLong(), any());
    }

    @Test
    void answer_throwsIllegalState_whenSubjectStatsRowIsMissing() {
        givenRoundWithQuestion(runningAttempt(user));
        when(subjectStatsRepository.addAnswer(eq(user), eq(subject), eq(1L), any())).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.answer(ATTEMPT_ID, 100L, "B", EMAIL));
    }

    // ---------- finish ----------

    // 4 questions: 2 correct, 1 wrong, 1 skipped.
    private List<AttemptQuestion> roundRows(Attempt attempt) {
        AttemptQuestion correct1 = new AttemptQuestion(attempt, question(100L), 0);
        AttemptQuestion correct2 = new AttemptQuestion(attempt, question(101L), 1);
        AttemptQuestion wrong = new AttemptQuestion(attempt, question(102L), 2);
        AttemptQuestion skipped = new AttemptQuestion(attempt, question(103L), 3);
        correct1.recordAnswer("B", true, LocalDateTime.now());
        correct2.recordAnswer("B", true, LocalDateTime.now());
        wrong.recordAnswer("A", false, LocalDateTime.now());
        return List.of(correct1, correct2, wrong, skipped);
    }

    @Test
    void finish_closesRound_andCountsCorrectOutOfAllQuestions() {
        Attempt attempt = runningAttempt(user);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(attempt));
        when(attemptQuestionRepository.findByAttemptIdOrderByPosition(ATTEMPT_ID)).thenReturn(roundRows(attempt));

        PracticeFinishResponse response = service.finish(ATTEMPT_ID, EMAIL);

        // Wrong and skipped answers add nothing, and never subtract.
        assertEquals(2, response.getCorrectAnswers());
        assertEquals(4, response.getTotalQuestions());
        assertTrue(attempt.isFinished());
        verify(attemptRepository).save(attempt);
    }

    @Test
    void finish_savesBridgePracticeResult_withServerCountedTotals() {
        Attempt attempt = runningAttempt(user);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(attempt));
        when(attemptQuestionRepository.findByAttemptIdOrderByPosition(ATTEMPT_ID)).thenReturn(roundRows(attempt));

        service.finish(ATTEMPT_ID, EMAIL);

        ArgumentCaptor<PracticeResult> result = ArgumentCaptor.forClass(PracticeResult.class);
        verify(practiceResultRepository).save(result.capture());
        assertEquals(user, result.getValue().getUser());
        assertEquals(subject, result.getValue().getSubject());
        assertEquals(2, result.getValue().getCorrectAnswers());
        assertEquals(4, result.getValue().getTotalQuestions());
        assertEquals(50, result.getValue().getScore());
        assertEquals(attempt.getFinishedAt(), result.getValue().getCompletedAt());
    }

    @Test
    void finish_withNoAnswers_closesRoundButSavesNoBridgeResult() {
        Attempt attempt = runningAttempt(user);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(attempt));
        when(attemptQuestionRepository.findByAttemptIdOrderByPosition(ATTEMPT_ID))
                .thenReturn(List.of(new AttemptQuestion(attempt, question(100L), 0)));

        PracticeFinishResponse response = service.finish(ATTEMPT_ID, EMAIL);

        assertEquals(0, response.getCorrectAnswers());
        assertEquals(1, response.getTotalQuestions());
        assertTrue(attempt.isFinished());
        verify(practiceResultRepository, never()).save(any());
    }

    @Test
    void finish_throwsConflict_whenRoundAlreadyFinished() {
        Attempt finished = runningAttempt(user);
        finished.setFinishedAt(LocalDateTime.now());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(finished));

        assertThrows(ConflictException.class, () -> service.finish(ATTEMPT_ID, EMAIL));
        verify(practiceResultRepository, never()).save(any());
    }

    @Test
    void finish_throwsAccessDenied_whenRoundBelongsToSomeoneElse() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(runningAttempt(user(2L))));

        assertThrows(AccessDeniedException.class, () -> service.finish(ATTEMPT_ID, EMAIL));
        verify(attemptRepository, never()).save(any());
    }
}
