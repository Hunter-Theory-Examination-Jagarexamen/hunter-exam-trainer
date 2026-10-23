package com.hunterexam.backend.service;

import com.hunterexam.backend.dto.QuestionRequest;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import(QuestionService.class)
class QuestionImagePersistenceTests {
    @Autowired QuestionService service;
    @Autowired QuestionRepository questions;
    @Autowired SubjectRepository subjects;
    @Autowired EntityManager entityManager;

    private QuestionRequest request(Long subjectId, String url) {
        return new QuestionRequest("Which animal?", "Fox", "Deer", "Moose", "Bear", "Fox", "", url, subjectId);
    }

    @Test
    void persistsMaximumLengthUrlReplacesAndClearsIt() {
        Subject subject = new Subject();
        subject.setName("Image test subject");
        subject.setDescription("Wildlife");
        Long subjectId = subjects.saveAndFlush(subject).getId();
        String url = "https://example.com/";
        url += "a".repeat(1000 - url.length());
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(request(subjectId, url)).isEmpty());
            assertFalse(factory.getValidator().validate(request(subjectId, url + "a")).isEmpty());
        }
        Long id = service.create(request(subjectId, url)).getId();
        questions.flush();
        entityManager.clear();
        assertEquals(url, questions.findById(id).orElseThrow().getImageUrl());
        service.update(id, request(subjectId, "https://example.com/replaced.jpg"));
        questions.flush();
        entityManager.clear();
        assertEquals("https://example.com/replaced.jpg", questions.findById(id).orElseThrow().getImageUrl());
        service.update(id, request(subjectId, null));
        questions.flush();
        entityManager.clear();
        assertNull(questions.findById(id).orElseThrow().getImageUrl());
    }
}
