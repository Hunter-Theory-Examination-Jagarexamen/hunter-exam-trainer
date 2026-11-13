package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamSubmitResponse;
import com.hunterexam.backend.dto.QuestionResponse;
import com.hunterexam.backend.dto.ExamStartResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.ExamTimeLimitExceededException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles the mock exam workflow.
 * <p>
 * This service generates randomized mock exam questions and evaluates
 * submitted exam answers, calculating the score and saving the result
 * for the logged-in user.
 * <p>
 * Security model: the server is the authority. The questions issued at
 * start are stored as the attempt's rows and reused on submit — the
 * client's list is ignored. Only the exam's owner may answer or submit.
 * <p>
 * Why this is a separate service from {@code PracticeAttemptService}:
 * both store their rounds in the same tables ({@code Attempt} +
 * {@code AttemptQuestion}), but the rules for answering are different.
 * <ul>
 *   <li><b>Practice:</b> each answer is checked right away, the student
 *       sees whether it was correct, the answer can't be changed, and it
 *       is added to the per-subject totals ({@code SubjectStats}) at once.</li>
 *   <li><b>Mock exam (this class):</b> like the real exam, the student can
 *       change an answer as often as they like until they submit, and sees
 *       nothing about correctness before that. The answers are added to
 *       the per-subject totals once, when the exam ends — at submit, or
 *       when it ends without a result (#187) — so a changed answer or a
 *       repeated ending isn't counted twice. There is also a time limit.</li>
 * </ul>
 * One shared service would need "if practice … else exam" checks in most
 * methods. Two services keep each set of rules in one readable place;
 * the cost is a little repeated code, such as looking up the student's
 * own running attempt.
 */
@Service
public class ExamService {

    private static final long EXAM_TIME_LIMIT_MINUTES = 60;

    /**
     * A short grace period after the time limit, so a submit that arrives
     * seconds late (slow connection, timer edge case) is still accepted.
     * It also bounds the answer window: only answers given at most
     * {@code limit + grace} minutes after the exam's start ever count
     * (#187), and the progress chart applies the same bound.
     */
    private static final long SUBMIT_GRACE_MINUTES = 2;

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AttemptRepository attemptRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;
    private final SubjectStatsRepository subjectStatsRepository;

    public ExamService(
            QuestionRepository questionRepository,
            UserRepository userRepository,
            AttemptRepository attemptRepository,
            AttemptQuestionRepository attemptQuestionRepository,
            SubjectStatsRepository subjectStatsRepository
    ) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.attemptQuestionRepository = attemptQuestionRepository;
        this.subjectStatsRepository = subjectStatsRepository;
    }

    /**
     * Starts a new mock exam for the given user.
     * <p>
     * Selects 70 questions at random and stores them as a {@code MOCK_EXAM}
     * attempt: one {@link AttemptQuestion} row per question, in the order
     * given. Returns them without the correct answers.
     * <p>
     * Any mock exam the student left running is closed first, without
     * scoring it (#186), so there is never more than one open exam. Its
     * answers given within the time limit still count (#187).
     *
     * @param email email of the logged-in user
     * @return the attempt id and the 70 questions
     */
    @Transactional
    public ExamStartResponse startExam(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Question> questions = questionRepository.findAll();

        if (questions.size() < 70) {
            throw new ConflictException(
                    "Not enough questions available to start the exam."
            );
        }

        // One open mock exam per student: an earlier exam left running (tab
        // closed, abandoned through the menu, ...) is closed without scoring
        // it — its in-window answers are counted once, through the same
        // ending as abandonment (#187) — so at most one exam can be running.
        // Same idea as practice start; only mock exams are closed, never
        // practice rounds.
        for (Attempt leftExam : attemptRepository.findByUserAndFinishedAtIsNullAndTypeIn(
                user, Set.of(AttemptType.MOCK_EXAM))) {
            endWithoutScore(leftExam);
        }

        // Randomize question order so each exam attempt is different.
        Collections.shuffle(questions);

        List<Question> chosen = questions.stream().limit(70).toList();

        // The attempt is the exam itself; its start time is what the
        // server's time limit is measured from.
        Attempt attempt = attemptRepository.save(
                new Attempt(user, AttemptType.MOCK_EXAM, LocalDateTime.now()));

        // One row per question, in the order the student sees them.
        // The answer fields stay empty until the student answers.
        List<AttemptQuestion> rows = new ArrayList<>();
        for (int position = 0; position < chosen.size(); position++) {
            rows.add(new AttemptQuestion(attempt, chosen.get(position), position));
        }
        attemptQuestionRepository.saveAll(rows);

        // Create the student's missing totals rows now, so submit only has to update them.
        ensureSubjectStatsExist(user, chosen.stream()
                .map(Question::getSubject)
                .collect(Collectors.toSet()));

        List<QuestionResponse> questionDtos = chosen.stream()
                .map(question -> new QuestionResponse(
                        question.getId(),
                        question.getQuestionText(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD(),
                        question.getImageUrl()
                ))
                .toList();

        return new ExamStartResponse(attempt.getId(), questionDtos);
    }


    /**
     * Saves (or changes) one answer in the given mock exam.
     * <p>
     * The exam must belong to the current user and not be submitted yet,
     * and the question must be part of it. Unlike practice, the same
     * question can be answered again: the new answer replaces the old one.
     * Whether it is correct is stored too, but never sent to the student
     * before submit. Nothing is added to the per-subject totals here; that
     * happens once, at submit.
     *
     * @param attemptId  the exam (its attempt id)
     * @param questionId question being answered
     * @param answer     the chosen letter, "A"–"D" (validated by the DTO)
     * @param email      email of the logged-in user
     */
    @Transactional
    public void saveAnswer(Long attemptId, Long questionId, String answer, String email) {

        Attempt exam = findOwnRunningExam(attemptId, email);

        AttemptQuestion row = attemptQuestionRepository
            .findByAttemptIdAndQuestionId(exam.getId(), questionId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Question " + questionId + " is not part of this exam."));

        boolean correct = answer.equals(row.getQuestion().getCorrectAnswer());
        row.recordAnswer(answer, correct, LocalDateTime.now());
        attemptQuestionRepository.save(row);
    }

    /**
     * Loads a mock exam and checks that it is the user's own and not submitted.
     * <p>
     * A practice round's id is treated as "not found": otherwise this endpoint
     * could change practice answers, which may only be given once.
     */
    private Attempt findOwnRunningExam(Long attemptId, String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

        Attempt exam = attemptRepository.findById(attemptId)
            .filter(attempt -> attempt.getType() == AttemptType.MOCK_EXAM)
            .orElseThrow(() -> new NotFoundException(
                "Mock exam not found with ID: " + attemptId));

        if (!exam.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this mock exam.");
        }
        if (exam.isFinished()) {
            throw new ConflictException("This mock exam has already been submitted.");
        }
        return exam;
    }

    /**
     * Ends a running mock exam without scoring it (#186), counting the
     * answers it holds within the time limit into the statistics (#187).
     * <p>
     * Called when the student confirms leaving through the menu or the
     * browser's back button. The exam is closed ({@code finishedAt} set,
     * {@code scored} stays false), so it can't be answered or submitted
     * again and never appears as a result or in scored-exam statistics —
     * but the answers given within the permitted window do count, exactly
     * like practice answers, in the per-subject totals.
     * <p>
     * {@link #endWithoutScore} is the one place an exam ends this way;
     * starting a new exam closes an earlier one the same way, a late
     * submit ends up there too, and future changes to ending an exam
     * without a result belong there too.
     *
     * @param attemptId the exam (its attempt id)
     * @param email     email of the logged-in user
     */
    @Transactional
    public void abandonExam(Long attemptId, String email) {
        endWithoutScore(findOwnRunningExam(attemptId, email));
    }

    /**
     * Closes an exam without scoring it: sets {@code finishedAt}, leaves
     * {@code scored} false (it is only ever set true by an in-time submit),
     * and counts the answers given within the time limit into the
     * per-subject totals (#187). Used by {@link #abandonExam}, by
     * {@link #startExam} for exams that were left running, and by
     * {@link #submitExam} for a submit that arrived too late.
     * <p>
     * The close is an atomic "only if still open" update
     * ({@link AttemptRepository#finishIfOpen}), and the counting happens in
     * the same transaction as the request that won it. A repeated or
     * concurrent ending of the same exam loses that update and counts
     * nothing, so every answer is counted exactly once no matter how many
     * times, or through which path, the exam is ended.
     */
    private void endWithoutScore(Attempt exam) {
        LocalDateTime now = LocalDateTime.now();

        if (attemptRepository.finishIfOpen(exam.getId(), now) == 0) {
            // Another request already ended this exam (and counted its
            // answers): do nothing rather than count them a second time.
            return;
        }

        exam.setFinishedAt(now);
        exam.setScored(false);
        countAnswersWithinTimeLimit(exam);
    }

    /**
     * Adds the exam's answers that fall within the permitted answer window
     * to the student's per-subject totals, keeping each answer's original
     * {@code answeredAt} so time-based statistics group it where it was
     * given.
     * <p>
     * The window is the exam's time limit ({@value #EXAM_TIME_LIMIT_MINUTES}
     * minutes) plus the same grace as submit ({@value #SUBMIT_GRACE_MINUTES}
     * minutes), measured from the exam's stored start — an answer counts
     * iff {@code answeredAt <= startedAt + limit + grace}, the same rule the
     * progress chart applies. Answers after that instant, and unanswered
     * questions, are not counted. Uses only server-side timestamps; the
     * client has no say in them.
     * <p>
     * Only used when an exam ends without a result: a scored submit counts
     * its answers itself, unchanged.
     */
    private void countAnswersWithinTimeLimit(Attempt exam) {
        LocalDateTime deadline = exam.getStartedAt()
                .plusMinutes(EXAM_TIME_LIMIT_MINUTES + SUBMIT_GRACE_MINUTES);

        for (AttemptQuestion row : attemptQuestionRepository.findByAttemptIdOrderByPosition(exam.getId())) {
            if (!row.isAnswered() || row.getAnsweredAt().isAfter(deadline)) {
                continue;
            }
            boolean correct = Boolean.TRUE.equals(row.getCorrect());
            addToSubjectStats(exam.getUser(), row.getQuestion().getSubject(), correct, row.getAnsweredAt());
        }
    }


    /**
     * Submits and scores a mock exam.
     * <p>
     * The score is counted from the answers stored on the exam's rows; the
     * client's request body (apart from the id) is ignored. Unanswered
     * questions count as not correct. Submitting closes the exam, so it
     * can't be answered or submitted again, and adds each answered question
     * to the student's per-subject totals.
     * <p>
     * A submit that arrives more than {@link #SUBMIT_GRACE_MINUTES} minutes
     * after the time limit ends the exam without a result instead: it is
     * closed and its in-window answers are counted (#187) through the same
     * {@link #endWithoutScore} as abandonment, but no score, pass/fail or
     * scored-exam entry is produced.
     * <p>
     * {@code noRollbackFor}: normally an exception undoes all database
     * changes in the method. A late submit should still close the exam and
     * keep the answers it counted, so that one exception keeps the changes.
     *
     * @param request the exam's attempt id
     * @param email   email of the logged-in user
     * @return the exam result, including score and answer breakdown
     */
    @Transactional(noRollbackFor = ExamTimeLimitExceededException.class)
    public ExamSubmitResponse submitExam(ExamSubmitRequest request, String email) {

        Long attemptId = request.getAttemptId();
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID is required for exam submission.");
        }

        Attempt exam = findOwnRunningExam(attemptId, email);
        LocalDateTime now = LocalDateTime.now();

        // The time limit counts from the exam's start, as stored by the server.
        // A short grace period lets a submit that arrives a few seconds late
        // (slow connection, timer edge case) still be accepted.
        long minutesElapsed = Duration.between(exam.getStartedAt(), now).toMinutes();

        if (minutesElapsed > EXAM_TIME_LIMIT_MINUTES + SUBMIT_GRACE_MINUTES) {
            // Too late for a result, but the exam still ends and its in-window
            // answers still count (#187) — through the shared ending, so the
            // answers can't be counted twice by a later abandonment or cleanup.
            endWithoutScore(exam);
            throw new ExamTimeLimitExceededException(
                "Exam submission failed: Time limit of " + EXAM_TIME_LIMIT_MINUTES
                    + " minutes exceeded by more than the " + SUBMIT_GRACE_MINUTES
                    + "-minute grace period."
            );
        }

        // In time: claim the closing transition. A concurrent abandonment or
        // submit that got there first already counted the answers, so this
        // one must not score the exam again.
        if (attemptRepository.finishIfOpen(exam.getId(), now) == 0) {
            throw new ConflictException("This mock exam has already been submitted.");
        }
        exam.setFinishedAt(now);

        // The exam counts as scored (saved at the end of the transaction).
        exam.setScored(true);

        List<AttemptQuestion> rows = attemptQuestionRepository.findByAttemptIdOrderByPosition(exam.getId());

        long totalQuestions = rows.size();
        long correctAnswers = 0;
        long incorrectAnswers = 0;

        for (AttemptQuestion row : rows) {
            if (!row.isAnswered()) {
                continue;  // skipped: counts as not correct, and isn't added to the totals
            }
            boolean correct = Boolean.TRUE.equals(row.getCorrect());
            if (correct) {
                correctAnswers++;
            } else {
                incorrectAnswers++;
            }
            addToSubjectStats(exam.getUser(), row.getQuestion().getSubject(), correct, row.getAnsweredAt());
        }

        long unanswered = totalQuestions - correctAnswers - incorrectAnswers;
        long score = Math.round(((double) correctAnswers / totalQuestions) * 100);

        return new ExamSubmitResponse(
                totalQuestions,
                correctAnswers,
                incorrectAnswers,
                unanswered,
                score
        );
    }

    /**
     * Creates the student's totals row (0 answered, 0 correct) for each subject
     * in the exam that doesn't have one yet.
     * <p>
     * Same idea as in {@code PracticeAttemptService}: done at start, so that
     * submit only ever updates rows and never inserts them.
     */
    private void ensureSubjectStatsExist(User user, Set<Subject> subjects) {
        for (Subject subject : subjects) {
            if (!subjectStatsRepository.existsByUserAndSubject(user, subject)) {
                subjectStatsRepository.save(new SubjectStats(user, subject));
            }
        }
    }

    /**
     * Adds one answer to the student's totals for the subject. The database does
     * the adding, so a practice answer at the same moment can't be overwritten.
     */
    private void addToSubjectStats(User user, Subject subject, boolean correct, LocalDateTime answeredAt) {
        int updated = subjectStatsRepository.addAnswer(user, subject, correct ? 1 : 0, answeredAt);
        if (updated == 0) {
            throw new IllegalStateException(
                    "No totals row for subject " + subject.getId() + "; it should have been created at start.");
        }
    }
}
