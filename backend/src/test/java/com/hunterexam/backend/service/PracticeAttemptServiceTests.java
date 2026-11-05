package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.PracticeAnswerResponse;
import com.hunterexam.backend.dto.PracticeFinishResponse;
import com.hunterexam.backend.dto.PracticeStartResponse;
import com.hunterexam.backend.dto.QuestionResponse;
import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * Unit tests for {@link PracticeAttemptService}: start, startRandom, answer
 * and finish.
 * <p>
 * The repositories are Mockito mocks, so no database is used.
 */
@ExtendWith(MockitoExtension.class)
class PracticeAttemptServiceTests {

    private static final String EMAIL = "student@example.test";
    private static final Long ATTEMPT_ID = 10L;

    @Mock AttemptRepository attemptRepository;
    @Mock AttemptQuestionRepository attemptQuestionRepository;
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
                questionRepository, subjectRepository,
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
        return question(id, subject);
    }

    private Question question(Long id, Subject subject) {
        Question q = new Question();
        q.setId(id);
        q.setQuestionText("Question " + id);
        q.setOptionA("One");
        q.setOptionB("Two");
        q.setOptionC("Three");
        q.setOptionD("Four");
        q.setCorrectAnswer("B");
        q.setExplanation("Because two.");
        q.setSubject(subject);
        return q;
    }

    private static Subject subject(Long id, String name) {
        Subject s = new Subject();
        s.setId(id);
        s.setName(name);
        return s;
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
        assertEquals("Viltvård", response.getSubjectName());
        assertEquals(2, response.getQuestions().size());
        // QuestionResponse has no correctAnswer field at all; the ids prove the order.
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
    void start_closesEarlierRunningRound() {
        Attempt leftRound = runningAttempt(user);
        AttemptQuestion answered = new AttemptQuestion(leftRound, question(100L), 0);
        answered.recordAnswer("B", true, LocalDateTime.now());

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findById(5L)).thenReturn(Optional.of(subject));
        when(questionRepository.findBySubjectId(5L)).thenReturn(List.of(question(100L)));
        when(attemptRepository.findByUserAndFinishedAtIsNullAndTypeIn(eq(user), any()))
                .thenReturn(List.of(leftRound));
        when(attemptQuestionRepository.findByAttemptIdOrderByPosition(ATTEMPT_ID)).thenReturn(List.of(answered));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(call -> call.getArgument(0));
        when(subjectStatsRepository.existsByUserAndSubject(user, subject)).thenReturn(true);

        service.start(5L, EMAIL);

        assertTrue(leftRound.isFinished());
    }

    @Test
    void start_closesOnlyPracticeRounds_neverAMockExam() {
        // Starting practice must not touch a running mock exam (#186): only
        // the practice types are asked for, so an open exam stays open.
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(subjectRepository.findById(5L)).thenReturn(Optional.of(subject));
        when(questionRepository.findBySubjectId(5L)).thenReturn(List.of(question(100L)));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(call -> call.getArgument(0));
        when(subjectStatsRepository.existsByUserAndSubject(user, subject)).thenReturn(true);

        service.start(5L, EMAIL);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Collection<AttemptType>> types = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(attemptRepository).findByUserAndFinishedAtIsNullAndTypeIn(eq(user), types.capture());
        assertFalse(types.getValue().contains(AttemptType.MOCK_EXAM),
                "starting practice must never close a running mock exam");
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

    // ---------- startRandom ----------

    // Stubs what both random start tests need: the user, the bank, and a
    // round that gets an id when saved (like the database would give it).
    private void givenRandomBank(List<Question> bank) {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(questionRepository.findAllWithSubject()).thenReturn(bank);
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(call -> {
            Attempt saved = call.getArgument(0);
            saved.setId(ATTEMPT_ID);
            return saved;
        });
        when(subjectStatsRepository.existsByUserAndSubject(eq(user), any(Subject.class))).thenReturn(true);
    }

    @Test
    void startRandom_takesTheWholeBank_whenItIsSmallerThanTwenty_andCoversEverySubject() {
        Subject other = subject(6L, "Jaktvård");
        List<Question> bank = List.of(
                question(100L), question(101L), question(102L, other), question(103L, other));
        givenRandomBank(bank);

        PracticeStartResponse response = service.startRandom(EMAIL);

        assertEquals(ATTEMPT_ID, response.getAttemptId());
        // A round that mixes subjects has no single subject name.
        assertNull(response.getSubjectName());
        // The bank is smaller than a full round, so every question is in it.
        assertEquals(4, response.getQuestions().size());
        assertEquals(
                bank.stream().map(Question::getId).collect(Collectors.toSet()),
                response.getQuestions().stream().map(QuestionResponse::getId).collect(Collectors.toSet()));

        ArgumentCaptor<Attempt> attempt = ArgumentCaptor.forClass(Attempt.class);
        verify(attemptRepository).save(attempt.capture());
        assertEquals(AttemptType.RANDOM_PRACTICE, attempt.getValue().getType());

        // One row per question, in the order they are shown, none answered yet.
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AttemptQuestion>> rows = ArgumentCaptor.forClass(List.class);
        verify(attemptQuestionRepository).saveAll(rows.capture());
        assertEquals(4, rows.getValue().size());
        for (int position = 0; position < 4; position++) {
            assertEquals(position, rows.getValue().get(position).getPosition());
            assertFalse(rows.getValue().get(position).isAnswered());
        }

        // Every subject of the round gets a totals row, so answers can update it.
        verify(subjectStatsRepository).existsByUserAndSubject(user, subject);
        verify(subjectStatsRepository).existsByUserAndSubject(user, other);
    }

    @Test
    void startRandom_takesTwentyQuestions_withoutDuplicates_andNotOnlyTheBiggestSubject() {
        Subject small = subject(6L, "Jaktvård");
        List<Question> bank = new ArrayList<>();
        for (long id = 100; id <= 124; id++) {
            bank.add(question(id));          // 25 questions of the default subject
        }
        bank.add(question(200L, small));     // one question of a small subject
        givenRandomBank(bank);

        PracticeStartResponse response = service.startRandom(EMAIL);

        List<Long> ids = response.getQuestions().stream().map(QuestionResponse::getId).toList();
        assertEquals(20, ids.size(), "a full bank gives a full round");
        assertEquals(20, new HashSet<>(ids).size(), "no question should appear twice");
        // The round-robin picks one question per subject first, so the small
        // subject is represented even though the big one could fill the round.
        assertTrue(ids.contains(200L));
        verify(subjectStatsRepository).existsByUserAndSubject(user, small);
    }

    @Test
    void startRandom_throwsConflict_whenQuestionBankIsEmpty() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(questionRepository.findAllWithSubject()).thenReturn(List.of());

        assertThrows(ConflictException.class, () -> service.startRandom(EMAIL));

        // Nothing is opened: no earlier round closed, no round saved.
        verify(attemptRepository, never()).findByUserAndFinishedAtIsNullAndTypeIn(any(), any());
        verify(attemptRepository, never()).save(any());
        verify(attemptQuestionRepository, never()).saveAll(anyList());
    }

    @Test
    void startRandom_closesEarlierRunningRound() {
        Attempt leftRound = runningAttempt(user);
        AttemptQuestion answered = new AttemptQuestion(leftRound, question(100L), 0);
        answered.recordAnswer("B", true, LocalDateTime.now());

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(questionRepository.findAllWithSubject()).thenReturn(List.of(question(101L)));
        when(attemptRepository.findByUserAndFinishedAtIsNullAndTypeIn(eq(user), any()))
                .thenReturn(List.of(leftRound));
        when(attemptQuestionRepository.findByAttemptIdOrderByPosition(ATTEMPT_ID))
                .thenReturn(List.of(answered));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(call -> call.getArgument(0));
        when(subjectStatsRepository.existsByUserAndSubject(user, subject)).thenReturn(true);

        service.startRandom(EMAIL);

        // One open practice round per student, whatever kind of round it was.
        assertTrue(leftRound.isFinished());
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
    void answer_inARandomRound_updatesTheTotalsOfThatQuestionsOwnSubject() {
        // A mixed round: the answered question belongs to another subject than
        // the one the student practised most, so the answer must count there.
        Subject other = subject(6L, "Jaktvård");
        Attempt randomRound = new Attempt(user, AttemptType.RANDOM_PRACTICE, LocalDateTime.now());
        randomRound.setId(ATTEMPT_ID);
        AttemptQuestion row = new AttemptQuestion(randomRound, question(100L, other), 0);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(randomRound));
        when(attemptQuestionRepository.findByAttemptIdAndQuestionId(ATTEMPT_ID, 100L))
                .thenReturn(Optional.of(row));
        when(subjectStatsRepository.addAnswer(eq(user), eq(other), eq(1L), any())).thenReturn(1);

        PracticeAnswerResponse response = service.answer(ATTEMPT_ID, 100L, "B", EMAIL);

        assertTrue(response.isCorrect());
        verify(subjectStatsRepository).addAnswer(eq(user), eq(other), eq(1L), any());
        verify(subjectStatsRepository, never()).addAnswer(eq(user), eq(subject), anyLong(), any());
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
    void finish_closesRound_andCountsCorrectAnsweredAndTotal() {
        Attempt attempt = runningAttempt(user);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(attempt));
        when(attemptQuestionRepository.findByAttemptIdOrderByPosition(ATTEMPT_ID)).thenReturn(roundRows(attempt));

        PracticeFinishResponse response = service.finish(ATTEMPT_ID, EMAIL);

        // Wrong and skipped answers add nothing, and never subtract.
        assertEquals(2, response.getCorrectAnswers());
        assertEquals(3, response.getAnsweredQuestions());
        assertEquals(4, response.getTotalQuestions());
        assertTrue(attempt.isFinished());
        verify(attemptRepository).save(attempt);
    }

    @Test
    void finish_withNoAnswers_closesRound() {
        Attempt attempt = runningAttempt(user);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(attempt));
        when(attemptQuestionRepository.findByAttemptIdOrderByPosition(ATTEMPT_ID))
                .thenReturn(List.of(new AttemptQuestion(attempt, question(100L), 0)));

        PracticeFinishResponse response = service.finish(ATTEMPT_ID, EMAIL);

        assertEquals(0, response.getCorrectAnswers());
        assertEquals(0, response.getAnsweredQuestions());
        assertEquals(1, response.getTotalQuestions());
        assertTrue(attempt.isFinished());
    }

    @Test
    void finish_throwsConflict_whenRoundAlreadyFinished() {
        Attempt finished = runningAttempt(user);
        finished.setFinishedAt(LocalDateTime.now());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(finished));

        assertThrows(ConflictException.class, () -> service.finish(ATTEMPT_ID, EMAIL));
        verify(attemptRepository, never()).save(any());
    }

    @Test
    void finish_throwsAccessDenied_whenRoundBelongsToSomeoneElse() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(attemptRepository.findById(ATTEMPT_ID)).thenReturn(Optional.of(runningAttempt(user(2L))));

        assertThrows(AccessDeniedException.class, () -> service.finish(ATTEMPT_ID, EMAIL));
        verify(attemptRepository, never()).save(any());
    }
}
