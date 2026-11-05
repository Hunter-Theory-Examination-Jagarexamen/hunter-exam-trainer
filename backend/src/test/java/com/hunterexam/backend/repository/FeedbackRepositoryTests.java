package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Feedback;
import com.hunterexam.backend.entity.FeedbackType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class FeedbackRepositoryTests {

    @Autowired FeedbackRepository feedback;
    @Autowired UserRepository users;
    @Autowired QuestionRepository questions;
    @Autowired SubjectRepository subjects;
    @Autowired EntityManager em;

    private User student;
    private Question question;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        student = new User();
        student.setFullName("Test Student");
        student.setEmail("feedback-student@example.test");
        student.setPassword("irrelevant");
        student.setRole(Role.STUDENT);
        student.setCreatedAt(now);
        users.saveAndFlush(student);

        Subject subject = new Subject();
        subject.setName("Feedback test subject");
        subject.setDescription("For the feedback tests");
        subjects.saveAndFlush(subject);

        question = new Question();
        question.setQuestionText("Original question text?");
        question.setOptionA("A");
        question.setOptionB("B");
        question.setOptionC("C");
        question.setOptionD("D");
        question.setCorrectAnswer("A");
        question.setSubject(subject);
        questions.saveAndFlush(question);
    }

    /** Saves, then empties the persistence context so the next read comes from the database. */
    private Feedback saveAndReload(Feedback item) {
        Long id = feedback.saveAndFlush(item).getId();
        em.clear();
        return feedback.findById(id).orElseThrow();
    }

    @Test
    void questionReportKeepsACopyOfTheQuestionText() {
        Feedback saved = feedback.saveAndFlush(
                new Feedback(student, FeedbackType.QUESTION, "Fel svar", question, now));

        question.setQuestionText("Edited question text?");
        questions.saveAndFlush(question);
        em.clear();

        assertEquals("Original question text?",
                feedback.findById(saved.getId()).orElseThrow().getQuestionTextSnapshot());
    }

    @Test
    void deletingTheQuestionKeepsTheReportWithoutALink() {
        Long id = feedback.saveAndFlush(
                new Feedback(student, FeedbackType.QUESTION, "Fel svar", question, now)).getId();
        em.clear();

        questions.deleteById(question.getId());
        questions.flush();
        em.clear();

        Feedback reloaded = feedback.findById(id).orElseThrow();
        assertNull(reloaded.getQuestion());
        assertEquals("Original question text?", reloaded.getQuestionTextSnapshot());
    }

    @Test
    void deletingTheSenderKeepsTheFeedbackWithoutASender() {
        Long id = feedback.saveAndFlush(
                new Feedback(student, FeedbackType.APP, "Bra app", null, now)).getId();
        em.clear();

        users.deleteById(student.getId());
        users.flush();
        em.clear();

        assertNull(feedback.findById(id).orElseThrow().getSender());
    }

    @Test
    void countsOnlyTheSendersItemsSinceTheGivenTime() {
        feedback.saveAndFlush(new Feedback(student, FeedbackType.APP, "Old", null, now.minusDays(2)));
        feedback.saveAndFlush(new Feedback(student, FeedbackType.APP, "New 1", null, now.minusHours(2)));
        feedback.saveAndFlush(new Feedback(student, FeedbackType.OTHER, "New 2", null, now.minusMinutes(5)));

        assertEquals(2, feedback.countBySenderAndCreatedAtAfter(student, now.minusDays(1)));
    }

    @Test
    void countsUnhandledItems() {
        Feedback handled = new Feedback(student, FeedbackType.APP, "Done", null, now);
        handled.setHandled(true);
        feedback.saveAndFlush(handled);
        saveAndReload(new Feedback(student, FeedbackType.APP, "Open", null, now));

        assertEquals(1, feedback.countByHandledFalse());
    }
}
