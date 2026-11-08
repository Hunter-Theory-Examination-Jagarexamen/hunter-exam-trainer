package com.hunterexam.backend.repository;

import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the query that closes exams left running (#186): only the given
 * user's open attempts of the given types come back, so starting a new exam
 * can never touch a scored exam, a finished exam, a practice round, or
 * another student's attempts.
 */
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class AttemptRepositoryTests {

    @Autowired AttemptRepository attempts;
    @Autowired UserRepository users;

    private final LocalDateTime now = LocalDateTime.now();
    private User student;

    @BeforeEach
    void setUp() {
        student = saveUser("student@example.test");
    }

    private User saveUser(String email) {
        User user = new User();
        user.setFullName(email);
        user.setEmail(email);
        user.setPassword("irrelevant");
        user.setRole(Role.STUDENT);
        user.setCreatedAt(now);
        return users.saveAndFlush(user);
    }

    private Attempt save(User user, AttemptType type, LocalDateTime finishedAt, boolean scored) {
        Attempt attempt = new Attempt(user, type, now.minusHours(1));
        attempt.setFinishedAt(finishedAt);
        attempt.setScored(scored);
        return attempts.saveAndFlush(attempt);
    }

    @Test
    void onlyTheUsersOpenExamsOfTheGivenTypesComeBack() {
        // Arrange: only the first attempt should come back
        Attempt open = save(student, AttemptType.MOCK_EXAM, null, false);
        save(student, AttemptType.MOCK_EXAM, now, true);            // scored: finished
        save(student, AttemptType.MOCK_EXAM, now, false);           // abandoned: finished
        save(student, AttemptType.SUBJECT_PRACTICE, null, false);   // open, but another type
        save(saveUser("other@example.test"), AttemptType.MOCK_EXAM, null, false); // another student

        // Act
        List<Attempt> openExams = attempts.findByUserAndFinishedAtIsNullAndTypeIn(
                student, Set.of(AttemptType.MOCK_EXAM));

        // Assert
        assertEquals(List.of(open), openExams);
    }
}
