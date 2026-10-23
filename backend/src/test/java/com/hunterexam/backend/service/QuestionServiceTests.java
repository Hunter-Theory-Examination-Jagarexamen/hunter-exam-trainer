package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.QuestionRequest;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTests {
    @Mock QuestionRepository questions;
    @Mock SubjectRepository subjects;

    private QuestionRequest request(String answer) {
        return new QuestionRequest("Updated question", "One", "Two", "Three", "Four", answer, "Why", null, 2L);
    }

    @Test
    void createsAndUpdatesAllFieldsWithoutReplacingId() {
        var service = new QuestionService(questions, subjects);
        Subject subject = new Subject();
        subject.setId(2L);
        when(subjects.findById(2L)).thenReturn(Optional.of(subject));
        when(questions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Question created = service.create(request("One"));
        assertNull(created.getId());
        assertSame(subject, created.getSubject());
        Question existing = new Question();
        existing.setId(7L);
        when(questions.findById(7L)).thenReturn(Optional.of(existing));
        Question updated = service.update(7L, request("Two"));
        assertSame(existing, updated);
        assertEquals(7L, updated.getId());
        assertEquals("Updated question", updated.getQuestionText());
        assertEquals("One", updated.getOptionA());
        assertEquals("Two", updated.getOptionB());
        assertEquals("Three", updated.getOptionC());
        assertEquals("Four", updated.getOptionD());
        assertEquals("Two", updated.getCorrectAnswer());
        assertEquals("Why", updated.getExplanation());
        assertNull(updated.getImageUrl());
        assertSame(subject, updated.getSubject());
    }

    @Test
    void rejectsAnswerOutsideOptionsAndUnknownSubject() {
        var service = new QuestionService(questions, subjects);
        assertThrows(IllegalArgumentException.class, () -> service.create(request("A")));
        assertThrows(NotFoundException.class, () -> service.create(request("One")));
        verify(questions, never()).save(any());
    }

    @Test
    void updateAndDeleteReturnNotFoundForMissingQuestion() {
        var service = new QuestionService(questions, subjects);
        assertThrows(NotFoundException.class, () -> service.update(7L, request("One")));
        assertThrows(NotFoundException.class, () -> service.delete(7L));
        verify(questions, never()).delete(any());
    }

    @Test
    void deletesExistingQuestion() {
        var question = new Question();
        when(questions.findById(7L)).thenReturn(Optional.of(question));
        new QuestionService(questions, subjects).delete(7L);
        verify(questions).delete(question);
    }
}
