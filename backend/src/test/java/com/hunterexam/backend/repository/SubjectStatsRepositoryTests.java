package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the JPQL query {@code findAnsweredByUser} against a real (H2) database,
 * which the Mockito tests in {@code StatisticsServiceTests} can't do.
 */
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class SubjectStatsRepositoryTests {

    @Autowired SubjectStatsRepository subjectStats;
    @Autowired SubjectRepository subjects;
    @Autowired UserRepository users;

    private User saveUser(String email) {
        User user = new User();
        user.setFullName(email);
        user.setEmail(email);
        user.setPassword("irrelevant");
        user.setRole(Role.STUDENT);
        user.setCreatedAt(LocalDateTime.now());
        return users.saveAndFlush(user);
    }

    private Subject saveSubject(String name) {
        Subject subject = new Subject();
        subject.setName(name);
        subject.setDescription(name);
        return subjects.saveAndFlush(subject);
    }

    private void saveStats(User user, Subject subject, long answered, long correct) {
        SubjectStats stats = new SubjectStats(user, subject);
        stats.setAnswered(answered);
        stats.setCorrect(correct);
        subjectStats.saveAndFlush(stats);
    }

    @Test
    void returnsOnlyThisStudentsSubjectsWithAnswers() {
        // Arrange: "student" answered in two subjects, started a round in a third
        // without answering; "other" has answers too
        User student = saveUser("student@example.test");
        User other = saveUser("other@example.test");
        Subject weapons = saveSubject("Weapons-test");
        Subject wildlife = saveSubject("Wildlife-test");
        Subject law = saveSubject("Law-test");
        saveStats(student, wildlife, 3, 1);
        saveStats(student, weapons, 20, 17);
        saveStats(student, law, 0, 0);
        saveStats(other, weapons, 5, 5);

        // Act
        List<SubjectStats> rows = subjectStats.findAnsweredByUser(student);

        // Assert: the 0-answer row and the other student's row are left out,
        // and the rows come ordered by subject id (Weapons was saved first)
        assertEquals(2, rows.size());
        assertEquals("Weapons-test", rows.get(0).getSubject().getName());
        assertEquals(17, rows.get(0).getCorrect());
        assertEquals("Wildlife-test", rows.get(1).getSubject().getName());
    }

    @Test
    void countAnsweredByUserSumsTheStudentsSubjectsAndIsZeroWithoutRows() {
        // Arrange
        User student = saveUser("student@example.test");
        User other = saveUser("other@example.test");
        User newStudent = saveUser("new@example.test");
        Subject weapons = saveSubject("Weapons-test");
        Subject wildlife = saveSubject("Wildlife-test");
        saveStats(student, weapons, 20, 17);
        saveStats(student, wildlife, 3, 1);
        saveStats(other, weapons, 5, 5);

        // Act + Assert: 20 + 3; the other student's 5 aren't counted;
        // no rows gives 0 (COALESCE), not null
        assertEquals(23, subjectStats.countAnsweredByUser(student));
        assertEquals(0, subjectStats.countAnsweredByUser(newStudent));
    }
}
