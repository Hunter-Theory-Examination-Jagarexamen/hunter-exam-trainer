package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.ExamResultResponse;
import com.hunterexam.backend.dto.ExamSubmitRequest;
import com.hunterexam.backend.entity.ExamSession;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.ExamResultRepository;
import com.hunterexam.backend.repository.ExamSessionRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for exam scoring in {@link ExamService#submitExam}.
 * The repositories are Mockito mocks, so no database is used.
 */
@ExtendWith(MockitoExtension.class)
class ExamServiceTests {

    private static final Long SESSION_ID = 1L;
    private static final String EMAIL = "student@example.test";

    @Mock QuestionRepository questionRepository;
    @Mock ExamResultRepository examResultRepository;
    @Mock UserRepository userRepository;
    @Mock ExamSessionRepository examSessionRepository;

    private ExamService examService;
    private User user;

    @BeforeEach
    void setUp() {
        examService = new ExamService(questionRepository, examResultRepository,
                userRepository, examSessionRepository);
        user = new User();
        user.setEmail(EMAIL);
    }

    /** A question whose correct answer is option B ("Two"). */
    private Question question(long id) {
        Question question = new Question();
        question.setId(id);
        question.setOptionA("One");
        question.setOptionB("Two");
        question.setOptionC("Three");
        question.setOptionD("Four");
        question.setCorrectAnswer("Two");
        return question;
    }

    /** Fakes an open session that started now, plus the questions and the user. */
    private void givenOpenSessionWith(List<Question> questions) {
        ExamSession session = new ExamSession(user, LocalDateTime.now());
        when(examSessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(questionRepository.findAllById(questions.stream().map(Question::getId).toList()))
                .thenReturn(questions);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private ExamSubmitRequest submission(List<Long> questionIds, Map<String, String> answers) {
        ExamSubmitRequest request = new ExamSubmitRequest();
        request.setSessionId(SESSION_ID);
        request.setQuestionIds(questionIds);
        request.setAnswers(answers);
        return request;
    }

    @Test
    void allCorrectAnswersGiveFullScore() {
        // Arrange
        givenOpenSessionWith(List.of(question(1), question(2), question(3), question(4)));
        ExamSubmitRequest request = submission(List.of(1L, 2L, 3L, 4L),
                Map.of("1", "B", "2", "B", "3", "B", "4", "B"));

        // Act
        ExamResultResponse result = examService.submitExam(request, EMAIL);

        // Assert
        assertEquals(4, result.getTotalQuestions());
        assertEquals(4, result.getCorrectAnswers());
        assertEquals(0, result.getIncorrectAnswers());
        assertEquals(0, result.getUnanswered());
        assertEquals(100, result.getScore());
    }
}
