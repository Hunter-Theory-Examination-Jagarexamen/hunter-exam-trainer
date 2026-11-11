package com.hunterexam.backend.service;

import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.Feedback;
import com.hunterexam.backend.entity.FeedbackType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.FeedbackRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountDeletionServiceTest {

    @Autowired private AccountDeletionService accountDeletionService;
    @Autowired private UserRepository userRepository;
    @Autowired private AttemptRepository attemptRepository;
    @Autowired private AttemptQuestionRepository attemptQuestionRepository;
    @Autowired private SubjectStatsRepository subjectStatsRepository;
    @Autowired private FeedbackRepository feedbackRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager entityManager;

    private static final String PASSWORD = "secret123";

    @BeforeEach
    void setUp() {
        // Delete the auto-created admin from DataInitializer
        userRepository.findByEmail("admin@example.test").ifPresent(userRepository::delete);
        entityManager.flush();
    }

    private User newStudent(String email) {
        return newUser(email, Role.STUDENT);
    }

    private User newAdmin(String email) {
        return newUser(email, Role.ADMIN);
    }

    private User newUser(String email, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setFullName("Test " + email);
        u.setPassword(passwordEncoder.encode(PASSWORD));
        u.setPasswordLoginEnabled(true);
        u.setRole(role);
        u.setCreatedAt(LocalDateTime.now());
        return userRepository.save(u);
    }

    private Subject newSubject(String name) {
        Subject s = new Subject();
        s.setName(name);
        s.setDescription("Description for " + name);
        return subjectRepository.save(s);
    }

    private Question newQuestion(Subject subject) {
        Question q = new Question();
        q.setQuestionText("Q?");
        q.setOptionA("A");
        q.setOptionB("B");
        q.setOptionC("C");
        q.setOptionD("D");
        q.setCorrectAnswer("A");
        q.setSubject(subject);
        return questionRepository.save(q);
    }

    private void seedPersonalData(User user) {
        Subject subject = newSubject("Test subject " + user.getEmail());
        Question question = newQuestion(subject);

        Attempt attempt = attemptRepository.save(
            new Attempt(user, AttemptType.MOCK_EXAM, LocalDateTime.now()));

        AttemptQuestion aq = new AttemptQuestion(attempt, question, 0);
        aq.recordAnswer("A", true, LocalDateTime.now());
        attemptQuestionRepository.save(aq);

        subjectStatsRepository.save(new SubjectStats(user, subject));
        feedbackRepository.save(new Feedback(
            user, FeedbackType.APP, "test message", null, LocalDateTime.now()));

        // Persist everything to DB so tests can find it
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void deleteOwnAccount_withWrongPassword_deletesNothing() {
        User user = newStudent("wrong@x.se");
        seedPersonalData(user);
        entityManager.flush();
        entityManager.clear();

        long attemptsBefore = attemptRepository.count();
        long statsBefore = subjectStatsRepository.count();

        assertThatThrownBy(() ->
            accountDeletionService.deleteOwnAccount(user.getEmail(), "not-the-password"))
            .isInstanceOf(IllegalArgumentException.class);

        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findById(user.getId())).isPresent();
        assertThat(attemptRepository.count()).isEqualTo(attemptsBefore);
        assertThat(subjectStatsRepository.count()).isEqualTo(statsBefore);
    }
    @Test
    void deleteOwnAccount_withCorrectPassword_deletesAllPersonalData() {
        User user = newStudent("own@x.se");
        seedPersonalData(user);
        entityManager.flush();
        entityManager.clear();
        Long userId = user.getId();

        accountDeletionService.deleteOwnAccount(user.getEmail(), PASSWORD);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findById(userId)).isEmpty();
        // Any attempts that pointed to this user are gone
        assertThat(attemptRepository.count()).isZero();
        assertThat(subjectStatsRepository.count()).isZero();
    }

    @Test
    void deleteOwnAccount_keepsFeedbackButRemovesSenderLink() {
        User user = newStudent("fb@x.se");
        seedPersonalData(user);
        entityManager.flush();
        entityManager.clear(); // Clear the Hibernate session

        long feedbackBefore = feedbackRepository.count();
        accountDeletionService.deleteOwnAccount(user.getEmail(), PASSWORD);

        entityManager.flush();
        entityManager.clear();

        assertThat(feedbackRepository.count()).isEqualTo(feedbackBefore);
        assertThat(feedbackRepository.findAll())
            .filteredOn(f -> "test message".equals(f.getText()))
            .singleElement()
            .satisfies(f -> assertThat(f.getSender()).isNull());
    }

    @Test
    void deleteOwnAccount_whenLastAdmin_isRejected() {
        User admin = newAdmin("last-admin@x.se");
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() ->
            accountDeletionService.deleteOwnAccount(admin.getEmail(), PASSWORD))
            .isInstanceOf(ConflictException.class);

        assertThat(userRepository.findById(admin.getId())).isPresent();
    }

    @Test
    void deleteOwnAccount_whenNotLastAdmin_succeeds() {
        newAdmin("admin-a@x.se");
        User second = newAdmin("admin-b@x.se");
        entityManager.flush();
        entityManager.clear();

        accountDeletionService.deleteOwnAccount(second.getEmail(), PASSWORD);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findById(second.getId())).isEmpty();
        assertThat(userRepository.countByRole(Role.ADMIN)).isEqualTo(1L);
    }

    @Test
    void deleteStudentAsAdmin_deletesStudent() {
        User student = newStudent("victim@x.se");
        seedPersonalData(student);
        entityManager.flush();
        entityManager.clear(); // Clear the Hibernate session
        Long studentId = student.getId();

        accountDeletionService.deleteStudentAsAdmin(studentId);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findById(studentId)).isEmpty();
    }

    @Test
    void deleteStudentAsAdmin_whenTargetIsAdmin_isRejected() {
        User admin = newAdmin("target-admin@x.se");
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() ->
            accountDeletionService.deleteStudentAsAdmin(admin.getId()))
            .isInstanceOf(AccessDeniedException.class);

        assertThat(userRepository.findById(admin.getId())).isPresent();
    }

    @Test
    void deleteStudentAsAdmin_whenTargetMissing_isRejected() {
        assertThatThrownBy(() ->
            accountDeletionService.deleteStudentAsAdmin(999_999_999L))
            .isInstanceOf(NotFoundException.class);
    }
}
