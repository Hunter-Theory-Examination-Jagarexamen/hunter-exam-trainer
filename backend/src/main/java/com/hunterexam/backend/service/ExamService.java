package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.QuestionResponse;
import com.hunterexam.backend.dto.ExamResultResponse;
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
 *       the per-subject totals only once, at submit, so a changed answer
 *       isn't counted twice. There is also a time limit.</li>
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
     * Submits and scores a mock exam.
     * <p>
     * The score is counted from the answers stored on the exam's rows; the
     * client's request body (apart from the id) is ignored. Unanswered
     * questions count as not correct. Submitting closes the exam, so it
     * can't be answered or submitted again, and adds each answered question
     * to the student's per-subject totals.
     * <p>
     * {@code noRollbackFor}: normally an exception undoes all database
     * changes in the method. A late submit should still close the exam,
     * so that one exception keeps the change.
     *
     * @param request the exam's attempt id
     * @param email   email of the logged-in user
     * @return the exam result, including score and answer breakdown
     */
    @Transactional(noRollbackFor = ExamTimeLimitExceededException.class)
    public ExamResultResponse submitExam(ExamSubmitRequest request, String email) {

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
        exam.setFinishedAt(now);
        attemptRepository.save(exam);

        if (minutesElapsed > EXAM_TIME_LIMIT_MINUTES + SUBMIT_GRACE_MINUTES) {
            throw new ExamTimeLimitExceededException(
                "Exam submission failed: Time limit of " + EXAM_TIME_LIMIT_MINUTES
                    + " minutes exceeded by more than the " + SUBMIT_GRACE_MINUTES
                    + "-minute grace period."
            );
        }

        // In time: the exam counts as scored (saved at the end of the transaction).
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

        return new ExamResultResponse(
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
