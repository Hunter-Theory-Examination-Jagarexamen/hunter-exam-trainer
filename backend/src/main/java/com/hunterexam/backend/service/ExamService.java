package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamQuestionResponse;
import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.entity.ExamResult;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

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
 */
@Service
public class ExamService {

    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;

    public ExamService(
            QuestionRepository questionRepository,
            ExamResultRepository examResultRepository,
            UserRepository userRepository
    ) {
        this.questionRepository = questionRepository;
        this.examResultRepository = examResultRepository;
        this.userRepository = userRepository;
    }

    /**
     * Starts a new mock exam.
     * <p>
     * Selects 70 questions at random from the full question bank.
     * Throws an exception if fewer than 70 questions are available.
     *
     * @return list of exam questions (without the correct answer included)
     */
    public List<ExamQuestionResponse> startExam() {

        List<Question> questions = questionRepository.findAll();

        if (questions.size() < 70) {
            throw new RuntimeException(
                    "Not enough questions available to start the exam."
            );
        }

        // Randomize question order so each exam attempt is different.
        Collections.shuffle(questions);

        return questions.stream()
                .limit(70)
                .map(question -> new ExamQuestionResponse(
                        question.getId(),
                        question.getQuestionText(),
                        question.getOptionA(),
                        question.getOptionB(),
                        question.getOptionC(),
                        question.getOptionD()
                ))
                .toList();
    }


    /**
     * Submits and evaluates a completed mock exam.
     * <p>
     * Compares the submitted answers against the correct answers, calculates
     * the number of correct, incorrect and unanswered questions, computes the
     * final score, and saves the result for the logged-in user.
     *
     * @param request exam submission containing the question IDs and the user's selected answers
     * @param email   user's email id
     * @return the exam result response, including score and answer breakdown
     */
    public ExamResultResponse submitExam(
            ExamSubmitRequest request,
            String email
    ) {

        List<Long> questionIds = request.getQuestionIds();
        Map<String, String> answers = request.getAnswers();

        if (questionIds == null || questionIds.isEmpty()) {
            throw new RuntimeException("No exam questions were submitted");
        }

        if (answers == null) {
            answers = Collections.emptyMap();
        }

        List<Question> questions = questionRepository.findAllById(questionIds);

        long totalQuestions = questionIds.size();
        long correctAnswers = 0;
        long incorrectAnswers = 0;

        for (Question question: questions) {

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

        // Find the logged-in user to associate the result with.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ExamResult examResult = new ExamResult();

        examResult.setTotalQuestions((int) totalQuestions);
        examResult.setCorrectAnswers((int) correctAnswers);
        examResult.setIncorrectAnswers((int) incorrectAnswers);
        examResult.setUnanswered((int) unanswered);
        examResult.setScore((int) score);
        examResult.setCompletedAt(LocalDateTime.now());
        examResult.setUser(user);

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
