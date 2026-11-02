package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.QuestionResponse;
import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamStartResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.ExamSession;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.ExamSessionRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;
    private final ExamSessionRepository examSessionRepository;
    private final AttemptRepository attemptRepository;
    private final AttemptQuestionRepository attemptQuestionRepository;

    public ExamService(
            QuestionRepository questionRepository,
            ExamResultRepository examResultRepository,
            UserRepository userRepository,
            ExamSessionRepository examSessionRepository,
            AttemptRepository attemptRepository,
            AttemptQuestionRepository attemptQuestionRepository
    ) {
        this.questionRepository = questionRepository;
        this.examResultRepository = examResultRepository;
        this.userRepository = userRepository;
        this.examSessionRepository = examSessionRepository;
        this.attemptRepository = attemptRepository;
        this.attemptQuestionRepository = attemptQuestionRepository;
    }

    /**
     * Starts a new mock exam for the given user.
     * <p>
     * Selects 70 questions at random and stores them as a {@code MOCK_EXAM}
     * attempt: one {@link AttemptQuestion} row per question, in the order
     * given. Returns them without the correct answers.
     *
     * @param email email of the logged-in user
     * @return the attempt id (sent as "sessionId") and the 70 questions
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

        // The page still calls it "sessionId"; it is now the attempt id.
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
     * @param attemptId  the exam (the page calls it "sessionId")
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
     * Submits and evaluates a completed mock exam.
     * <p>
     * The session's stored question ids are used for scoring — the client's
     * question list is ignored. Only the session's owner may submit.
     *
     * @param request exam submission containing the session id and answers
     * @param email   email of the logged-in user
     * @return the exam result, including score and answer breakdown
     */
    public ExamResultResponse submitExam(
            ExamSubmitRequest request,
            String email
    ) {

        Long sessionId = request.getSessionId();
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID is required for exam submission.");
        }

        User currentUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("Exam session not found with ID: " + sessionId));

        // Ownership check: only the session owner may submit it.
        if (session.getUser() == null
            || !session.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                "You do not own this exam session."
            );
        }

        if (session.isCompleted()) {
            throw new IllegalArgumentException("This exam session has already been submitted.");
        }

        long minutesElapsed = Duration.between(session.getStartedAt(), LocalDateTime.now()).toMinutes();

        // Allow a short grace period so a submit that arrives a few seconds
        // late (slow connection, timer edge case) is still accepted.
        if (minutesElapsed > EXAM_TIME_LIMIT_MINUTES + SUBMIT_GRACE_MINUTES) {
            session.setCompleted(true);
            examSessionRepository.save(session);
            throw new IllegalArgumentException(
                "Exam submission failed: Time limit of " + EXAM_TIME_LIMIT_MINUTES
                    + " minutes exceeded by more than the " + SUBMIT_GRACE_MINUTES
                    + "-minute grace period."
            );
        }

        session.setCompleted(true);
        examSessionRepository.save(session);

        // Use the questions the server issued — the client's list is ignored.
        List<Long> questionIds = session.getQuestionIds();
        if (questionIds == null || questionIds.isEmpty()) {
            throw new RuntimeException("No exam questions were issued for this session.");
        }

        // Use the answers the server recorded for this session.
        // The client's request body is intentionally ignored.
        Map<Long, String> answers = session.getAnswers();
        if (answers == null) {
            answers = Collections.emptyMap();
        }

        List<Question> questions = questionRepository.findAllById(questionIds);

        long totalQuestions = questionIds.size();
        long correctAnswers = 0;
        long incorrectAnswers = 0;

        for (Question question : questions) {

            // Answers are keyed by question id (Long), not String.
            String selectedAnswer = answers.get(question.getId());

            // Question was not answered by the user.
            if (selectedAnswer == null) {
                continue;
            }

            // Both selectedAnswer (from the client: "A"/"B"/"C"/"D") and
            // correctAnswer (from the question) are letters.

            if (selectedAnswer.equals(question.getCorrectAnswer())) {
                correctAnswers++;
            }
            else {
                incorrectAnswers++;
            }
        }

        // Any question not accounted for as correct/incorrect was left unanswered.
        long unanswered = totalQuestions - correctAnswers - incorrectAnswers;

        long score = Math.round(
                ((double) correctAnswers / totalQuestions) * 100
        );

        ExamResult examResult = new ExamResult();

        examResult.setTotalQuestions((int) totalQuestions);
        examResult.setCorrectAnswers((int) correctAnswers);
        examResult.setIncorrectAnswers((int) incorrectAnswers);
        examResult.setUnanswered((int) unanswered);
        examResult.setScore((int) score);
        examResult.setCompletedAt(LocalDateTime.now());
        examResult.setUser(currentUser);

        examResultRepository.save(examResult);

        return new ExamResultResponse(
                totalQuestions,
                correctAnswers,
                incorrectAnswers,
                unanswered,
                score
        );
    }
}
