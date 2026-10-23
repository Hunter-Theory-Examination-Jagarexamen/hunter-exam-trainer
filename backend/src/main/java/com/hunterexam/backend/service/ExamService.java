package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamQuestionResponse;
import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamStartResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.ExamSession;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.ExamSessionRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
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

    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;
    private final ExamSessionRepository examSessionRepository;

    public ExamService(
            QuestionRepository questionRepository,
            ExamResultRepository examResultRepository,
            UserRepository userRepository,
            ExamSessionRepository examSessionRepository
    ) {
        this.questionRepository = questionRepository;
        this.examResultRepository = examResultRepository;
        this.userRepository = userRepository;
        this.examSessionRepository = examSessionRepository;
    }

    /**
     * Starts a new mock exam for the given user.
     * <p>
     * Selects 70 questions at random, records which questions were issued
     * on the exam session, and returns them without the correct answers.
     *
     * @param email email of the logged-in user
     * @return session id and the 70 questions for this attempt
     */
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
        List<Long> chosenIds = chosen.stream().map(Question::getId).toList();

        ExamSession session = new ExamSession(user, LocalDateTime.now());
        session.setQuestionIds(chosenIds);
        ExamSession savedSession = examSessionRepository.save(session);

        List<ExamQuestionResponse> questionDtos = chosen.stream()
                .map(question -> new ExamQuestionResponse(
                        question.getId(),
                        question.getQuestionText(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD(),
                        question.getImageUrl()
                ))
                .toList();

        return new ExamStartResponse(savedSession.getId(), questionDtos);
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

        if (minutesElapsed > EXAM_TIME_LIMIT_MINUTES) {
            session.setCompleted(true);
            examSessionRepository.save(session);
            throw new IllegalArgumentException(
                    "Exam submission failed: Time limit of " + EXAM_TIME_LIMIT_MINUTES + " minutes exceeded."
            );
        }

        session.setCompleted(true);
        examSessionRepository.save(session);

        // Use the questions the server issued the client's list is ignored.
        List<Long> questionIds = session.getQuestionIds();
        if (questionIds == null || questionIds.isEmpty()) {
            throw new RuntimeException("No exam questions were issued for this session.");
        }

        Map<String, String> answers = request.getAnswers();
        if (answers == null) {
            answers = Collections.emptyMap();
        }

        List<Question> questions = questionRepository.findAllById(questionIds);

        long totalQuestions = questionIds.size();
        long correctAnswers = 0;
        long incorrectAnswers = 0;

        for (Question question : questions) {

            String selectedAnswer = answers.get(question.getId().toString());

            // Question was not answered by the user.
            if (selectedAnswer == null) {
                continue;
            }

            // Map the selected option letter (A/B/C/D) to its answer text.
            String selectedAnswerText = switch (selectedAnswer) {
                case "A" -> question.getOptionA();
                case "B" -> question.getOptionB();
                case "C" -> question.getOptionC();
                case "D" -> question.getOptionD();
                default -> null;
            };

            if (selectedAnswerText == null) {
                // Selected answer did not match any known option (e.g. invalid value submitted).
                incorrectAnswers++;
            }
            else if (selectedAnswerText.equals(question.getCorrectAnswer())) {
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