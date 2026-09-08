package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamQuestionResponse;
import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ExamService {

    private final QuestionRepository questionRepository;

    public ExamService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<ExamQuestionResponse> startExam() {

        List<Question> questions = questionRepository.findAll();

        if (questions.size() < 70) {
            throw new RuntimeException(
                    "Not enough questions available to start the exam."
            );
        }

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

    public ExamResultResponse submitExam(ExamSubmitRequest request) {

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

            if (selectedAnswer == null) {
                continue;
            }

            String selectedAnswerText = switch (selectedAnswer) {
                case "A" -> question.getOptionA();
                case "B" -> question.getOptionB();
                case "C" -> question.getOptionC();
                case "D" -> question.getOptionD();
                default -> null;
            };

            if (selectedAnswerText == null) {
                incorrectAnswers++;
            }
            else if (selectedAnswerText.equals(question.getCorrectAnswer())) {
                correctAnswers++;
            }
            else {
                incorrectAnswers++;
            }
        }

        long unanswered = totalQuestions - correctAnswers - incorrectAnswers;

        long score = Math.round(
                ((double) correctAnswers / totalQuestions) * 100
        );

        return new ExamResultResponse(
                totalQuestions,
                correctAnswers,
                incorrectAnswers,
                unanswered,
                score
        );
    }
}
