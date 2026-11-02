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
 * Security model: the server is the authority. Question ids issued at
 * start are stored on the session and reused on submit — the client's
 * list is ignored. Only the session owner may submit.
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
     * Saves a single answer on the given exam session.
     * <p>
     * The session must belong to the current user, must still be open,
     * and the question must have been issued for this session. Storing
     * answers server-side means a lost connection, expired token, or
     * closed tab does not lose the exam attempt.
     *
     * @param sessionId  exam session id
     * @param questionId question being answered
     * @param answer     the user's chosen option (A/B/C/D)
     * @param email      email of the logged-in user
     */
    public void saveAnswer(Long sessionId, Long questionId, String answer, String email) {

        User currentUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

        ExamSession session = examSessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException(
                "Exam session not found with ID: " + sessionId));

        if (session.getUser() == null
            || !session.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not own this exam session.");
        }

        if (session.isCompleted()) {
            throw new ConflictException("This exam session has already been submitted.");
        }

        if (!session.getQuestionIds().contains(questionId)) {
            throw new IllegalArgumentException(
                "Question " + questionId + " was not issued for this session.");
        }

        session.setAnswer(questionId, answer);
        examSessionRepository.save(session);
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
