package com.hunterexam.backend.controller;

import com.hunterexam.backend.entity.Attempt;
import com.hunterexam.backend.entity.AttemptQuestion;
import com.hunterexam.backend.entity.AttemptType;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.entity.Role;
import com.hunterexam.backend.entity.Subject;
import com.hunterexam.backend.entity.SubjectStats;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.repository.AttemptQuestionRepository;
import com.hunterexam.backend.repository.AttemptRepository;
import com.hunterexam.backend.repository.QuestionRepository;
import com.hunterexam.backend.repository.SubjectRepository;
import com.hunterexam.backend.repository.SubjectStatsRepository;
import com.hunterexam.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Student-controlled result sharing (#125) against the real endpoints and the
 * H2 test database: the preferences endpoints only ever touch the account the
 * token belongs to, and the admin's learner endpoint must not contain a result
 * the student has not shared — not as a value, not as a total, not as a
 * timestamp.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentResultSharingIntegrationTests {

    private static final String SUBJECT_NAME = "Sharing-Test-Subject";
    private static final String ADMIN_EMAIL = "sharing-admin@example.test";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired SubjectRepository subjects;
    @Autowired QuestionRepository questions;
    @Autowired AttemptRepository attempts;
    @Autowired AttemptQuestionRepository attemptQuestions;
    @Autowired SubjectStatsRepository subjectStats;
    @Autowired PasswordEncoder encoder;

    private Subject subject;

    @BeforeEach
    void setUp() {
        subject = new Subject();
        subject.setName(SUBJECT_NAME);
        subject.setDescription("Seeded by StudentResultSharingIntegrationTests");
        subjects.saveAndFlush(subject);
    }

    // ----- fixtures -----

    private User newStudent(String email) {
        User user = new User();
        user.setFullName("Sharing " + email);
        user.setEmail(email);
        user.setPassword(encoder.encode("secret123"));
        user.setRole(Role.STUDENT);
        user.setCreatedAt(LocalDateTime.now());
        // No sharing flags set: the test exercises the private default.
        return users.saveAndFlush(user);
    }

    /** One subject with 10/8 answered (80 %) and one scored mock exam, 1 of 2 correct (50 %). */
    private void giveResults(User student) {
        SubjectStats stats = new SubjectStats(student, subject);
        stats.setAnswered(10);
        stats.setCorrect(8);
        stats.setLastAnsweredAt(LocalDateTime.now().minusDays(1));
        subjectStats.saveAndFlush(stats);

        LocalDateTime examTime = LocalDateTime.now().minusDays(2);
        Attempt exam = new Attempt(student, AttemptType.MOCK_EXAM, examTime.minusMinutes(30));
        exam.setFinishedAt(examTime);
        exam.setScored(true);
        attempts.saveAndFlush(exam);

        AttemptQuestion correct = new AttemptQuestion(exam, newQuestion("First"), 0);
        correct.recordAnswer("A", true, examTime.minusMinutes(20));
        AttemptQuestion wrong = new AttemptQuestion(exam, newQuestion("Second"), 1);
        wrong.recordAnswer("B", false, examTime.minusMinutes(10));
        attemptQuestions.saveAll(List.of(correct, wrong));
        attemptQuestions.flush();
    }

    private Question newQuestion(String text) {
        Question question = new Question();
        question.setQuestionText(text + " question of " + SUBJECT_NAME);
        question.setOptionA("A");
        question.setOptionB("B");
        question.setOptionC("C");
        question.setOptionD("D");
        question.setCorrectAnswer("A");
        question.setSubject(subject);
        return questions.saveAndFlush(question);
    }

    private static RequestPostProcessor studentAuth(String email) {
        return jwt().jwt(token -> token.subject(email))
                .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    private static RequestPostProcessor adminAuth() {
        return jwt().jwt(token -> token.subject(ADMIN_EMAIL))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private void putSharing(RequestPostProcessor auth, String body) throws Exception {
        mvc.perform(put("/api/users/me/result-sharing")
                        .with(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    // ----- the preferences endpoints -----

    @Test
    void newStudentStartsPrivateAndKeepsItAcrossRequests() throws Exception {
        User student = newStudent("private@example.test");
        giveResults(student);

        mvc.perform(get("/api/users/me/result-sharing").with(studentAuth(student.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareSubjectStatistics").value(false))
                .andExpect(jsonPath("$.shareMockExamResults").value(false));

        // Still private on a later, separate request.
        mvc.perform(get("/api/users/me/result-sharing").with(studentAuth(student.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareSubjectStatistics").value(false))
                .andExpect(jsonPath("$.shareMockExamResults").value(false));
    }

    @Test
    void updatedPreferencesArePersistedAndReflectedImmediately() throws Exception {
        User student = newStudent("persist@example.test");
        giveResults(student);

        putSharing(studentAuth(student.getEmail()),
                "{\"shareSubjectStatistics\":true,\"shareMockExamResults\":false}");

        mvc.perform(get("/api/users/me/result-sharing").with(studentAuth(student.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareSubjectStatistics").value(true))
                .andExpect(jsonPath("$.shareMockExamResults").value(false));

        // The admin view reflects the saved choice in the very next request.
        mvc.perform(get("/api/admin/learners/" + student.getId()).with(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectPerformances[0].percentage").value(80))
                .andExpect(jsonPath("$.mockExams").value(nullValue()));
    }

    @Test
    void bothFlagsAreRequired() throws Exception {
        newStudent("missing@example.test");

        mvc.perform(put("/api/users/me/result-sharing")
                        .with(studentAuth("missing@example.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void studentCannotChangeAnotherStudentsPreferences() throws Exception {
        User alice = newStudent("alice@example.test");
        User bob = newStudent("bob@example.test");
        giveResults(bob);

        // Alice tries to write Bob's id into the body: the owner comes from
        // the token, so the extra field changes nothing but her own record.
        mvc.perform(put("/api/users/me/result-sharing")
                        .with(studentAuth(alice.getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shareSubjectStatistics\":true,\"shareMockExamResults\":true,"
                                + "\"userId\":" + bob.getId() + "}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/users/me/result-sharing").with(studentAuth(alice.getEmail())))
                .andExpect(jsonPath("$.shareSubjectStatistics").value(true))
                .andExpect(jsonPath("$.shareMockExamResults").value(true));

        mvc.perform(get("/api/users/me/result-sharing").with(studentAuth(bob.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shareSubjectStatistics").value(false))
                .andExpect(jsonPath("$.shareMockExamResults").value(false));

        // Bob's results stay hidden from the admin too.
        mvc.perform(get("/api/admin/learners/" + bob.getId()).with(adminAuth()))
                .andExpect(jsonPath("$.subjectPerformances").value(nullValue()))
                .andExpect(jsonPath("$.mockExams").value(nullValue()));
    }

    // ----- the admin endpoint -----

    @Test
    void hiddenResultsAreAbsentFromTheAdminResponse() throws Exception {
        User student = newStudent("hidden@example.test");
        giveResults(student);

        mvc.perform(get("/api/admin/learners/" + student.getId()).with(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectPerformances").value(nullValue()))
                .andExpect(jsonPath("$.mockExams").value(nullValue()))
                // Activity is derived from the hidden answers, so it is hidden too.
                .andExpect(jsonPath("$.lastActivityAt").value(nullValue()))
                // The subject's name would leak the practice results.
                .andExpect(content().string(not(containsString(SUBJECT_NAME))));
    }

    @Test
    void sharedResultsAreReturned() throws Exception {
        User student = newStudent("shared@example.test");
        giveResults(student);
        putSharing(studentAuth(student.getEmail()),
                "{\"shareSubjectStatistics\":true,\"shareMockExamResults\":true}");

        mvc.perform(get("/api/admin/learners/" + student.getId()).with(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectPerformances.length()").value(1))
                .andExpect(jsonPath("$.subjectPerformances[0].subjectName").value(SUBJECT_NAME))
                .andExpect(jsonPath("$.subjectPerformances[0].percentage").value(80))
                .andExpect(jsonPath("$.mockExams.count").value(1))
                .andExpect(jsonPath("$.mockExams.latestScore").value(50))
                .andExpect(jsonPath("$.mockExams.bestScore").value(50))
                .andExpect(jsonPath("$.lastActivityAt").isNotEmpty());
    }

    @Test
    void partiallySharedExposesOnlyThePermittedCategory() throws Exception {
        User student = newStudent("partial@example.test");
        giveResults(student);

        // Subject statistics shared, mock exams hidden.
        putSharing(studentAuth(student.getEmail()),
                "{\"shareSubjectStatistics\":true,\"shareMockExamResults\":false}");
        mvc.perform(get("/api/admin/learners/" + student.getId()).with(adminAuth()))
                .andExpect(jsonPath("$.subjectPerformances[0].percentage").value(80))
                .andExpect(jsonPath("$.mockExams").value(nullValue()))
                .andExpect(jsonPath("$.lastActivityAt").isNotEmpty());

        // The other way around: the exam summary shows, the subjects disappear.
        putSharing(studentAuth(student.getEmail()),
                "{\"shareSubjectStatistics\":false,\"shareMockExamResults\":true}");
        mvc.perform(get("/api/admin/learners/" + student.getId()).with(adminAuth()))
                .andExpect(jsonPath("$.subjectPerformances").value(nullValue()))
                .andExpect(jsonPath("$.mockExams.count").value(1))
                .andExpect(jsonPath("$.mockExams.latestScore").value(50))
                .andExpect(jsonPath("$.lastActivityAt").isNotEmpty())
                .andExpect(content().string(not(containsString(SUBJECT_NAME))));
    }

    // ----- students keep their own access -----

    @Test
    void studentsStillSeeTheirOwnResultsWhileEverythingIsHidden() throws Exception {
        User student = newStudent("own@example.test");
        giveResults(student);

        mvc.perform(get("/api/statistics/subjects").with(studentAuth(student.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].subjectName").value(SUBJECT_NAME))
                .andExpect(jsonPath("$[0].percentage").value(80));
    }
}
