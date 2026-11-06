package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.FeedbackRequest;
import com.hunterexam.backend.entity.Feedback;
import com.hunterexam.backend.entity.FeedbackType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.exception.TooManyRequestsException;
import com.hunterexam.backend.repository.FeedbackRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTests {

    private static final String EMAIL = "student@example.test";

    @Mock FeedbackRepository feedbackRepository;
    @Mock UserRepository userRepository;
    @Mock QuestionRepository questionRepository;

    private FeedbackService service;
    private User student;

    @BeforeEach
    void setUp() {
        service = new FeedbackService(feedbackRepository, userRepository, questionRepository);
        student = new User();
        student.setId(1L);
        student.setEmail(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(student));
    }

    private FeedbackRequest request(FeedbackType type, String text, Long questionId) {
        FeedbackRequest r = new FeedbackRequest();
        r.setType(type);
        r.setText(text);
        r.setQuestionId(questionId);
        return r;
    }

    private Feedback savedFeedback() {
        ArgumentCaptor<Feedback> saved = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository).save(saved.capture());
        return saved.getValue();
    }

    @Test
    void generalFeedbackIsSavedWithSenderAndNoQuestion() {
        service.send(EMAIL, request(FeedbackType.APP, "  Bra app  ", null));

        Feedback saved = savedFeedback();
        assertSame(student, saved.getSender());
        assertEquals(FeedbackType.APP, saved.getType());
        assertEquals("Bra app", saved.getText());   // spaces around the text are removed
        assertNull(saved.getQuestion());
        assertEquals(false, saved.isHandled());
    }

    @Test
    void questionReportLinksTheQuestionAndCopiesItsText() {
        Question question = new Question();
        question.setId(12L);
        question.setQuestionText("Vilket djur?");
        when(questionRepository.findById(12L)).thenReturn(Optional.of(question));

        service.send(EMAIL, request(FeedbackType.QUESTION, "Fel svar", 12L));

        Feedback saved = savedFeedback();
        assertSame(question, saved.getQuestion());
        assertEquals("Vilket djur?", saved.getQuestionTextSnapshot());
    }

    @Test
    void twentiethItemInADayIsStillAllowed() {
        when(feedbackRepository.countBySenderAndCreatedAtAfter(eq(student), any())).thenReturn(19L);

        service.send(EMAIL, request(FeedbackType.APP, "Hej", null));

        verify(feedbackRepository).save(any());
    }

    @Test
    void twentyFirstItemInADayIsRefused() {
        when(feedbackRepository.countBySenderAndCreatedAtAfter(eq(student), any())).thenReturn(20L);

        assertThrows(TooManyRequestsException.class,
                () -> service.send(EMAIL, request(FeedbackType.APP, "Hej", null)));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void unknownQuestionGivesNotFound() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.send(EMAIL, request(FeedbackType.QUESTION, "Fel", 99L)));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void questionIdWithAnotherTypeIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> service.send(EMAIL, request(FeedbackType.APP, "Hej", 12L)));
        verify(feedbackRepository, never()).save(any());
    }
}
