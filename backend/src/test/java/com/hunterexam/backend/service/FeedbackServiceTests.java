package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.FeedbackRequest;
import com.hunterexam.backend.dto.FeedbackResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    }

    /** For the send() tests: the logged-in student exists. */
    private void studentExists() {
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
        studentExists();
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
        studentExists();
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
        studentExists();
        when(feedbackRepository.countBySenderAndCreatedAtAfter(eq(student), any())).thenReturn(19L);

        service.send(EMAIL, request(FeedbackType.APP, "Hej", null));

        verify(feedbackRepository).save(any());
    }

    @Test
    void twentyFirstItemInADayIsRefused() {
        studentExists();
        when(feedbackRepository.countBySenderAndCreatedAtAfter(eq(student), any())).thenReturn(20L);

        assertThrows(TooManyRequestsException.class,
                () -> service.send(EMAIL, request(FeedbackType.APP, "Hej", null)));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void unknownQuestionGivesNotFound() {
        studentExists();
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.send(EMAIL, request(FeedbackType.QUESTION, "Fel", 99L)));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void questionIdWithAnotherTypeIsRefused() {
        studentExists();
        assertThrows(IllegalArgumentException.class,
                () -> service.send(EMAIL, request(FeedbackType.APP, "Hej", 12L)));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void adminResponseHasTheQuestionIdOrEmptyWhenTheQuestionIsDeleted() {
        Question question = new Question();
        question.setId(12L);
        question.setQuestionText("Vilket djur?");
        Feedback linked = new Feedback(student, FeedbackType.QUESTION, "Fel", question, LocalDateTime.now());
        Feedback deleted = new Feedback(student, FeedbackType.QUESTION, "Fel", question, LocalDateTime.now());
        deleted.setQuestion(null);   // what the database does when the question is deleted
        when(feedbackRepository.findForAdmin(null, null)).thenReturn(List.of(linked, deleted));

        List<FeedbackResponse> result = service.listForAdmin(null, null);

        assertEquals(12L, result.get(0).questionId());
        assertNull(result.get(1).questionId());
        assertEquals("Vilket djur?", result.get(1).questionText());
    }

    @Test
    void setHandledChangesTheFlag() {
        Feedback item = new Feedback(student, FeedbackType.APP, "Hej", null, LocalDateTime.now());
        when(feedbackRepository.findById(3L)).thenReturn(Optional.of(item));

        FeedbackResponse result = service.setHandled(3L, true);

        assertTrue(item.isHandled());
        assertTrue(result.handled());
    }

    @Test
    void setHandledOnUnknownItemGivesNotFound() {
        when(feedbackRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.setHandled(99L, true));
    }
}
