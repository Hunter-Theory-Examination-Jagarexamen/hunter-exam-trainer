package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.dto.ExamQuestionResponse;
import com.hunterexam.backend.dto.PracticeAnswerResponse;
import com.hunterexam.backend.dto.PracticeFinishResponse;
import com.hunterexam.backend.dto.PracticeStartResponse;
import com.hunterexam.backend.exception.ConflictException;
import com.hunterexam.backend.exception.NotFoundException;
import com.hunterexam.backend.service.PracticeAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link PracticeAttemptController}: the real SecurityConfig,
 * controller, validation and GlobalExceptionHandler run, but the service is a
 * mock. Checks URLs, status codes, validation and the JSON shape; the rules
 * themselves are tested in PracticeAttemptServiceTests.
 */
@WebMvcTest(PracticeAttemptController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class PracticeAttemptControllerTests {

    private static final String EMAIL = "student@example.test";

    @Autowired MockMvc mvc;
    @MockitoBean PracticeAttemptService practiceAttemptService;
    // Needed by SecurityConfig; not used in these tests
    @MockitoBean OAuth2LoginSuccessHandler loginHandler;

    // A fake valid token for a student; its subject becomes authentication.getName().
    private static RequestPostProcessor student() {
        return jwt().jwt(token -> token.subject(EMAIL))
                .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    // ---------- start ----------

    @Test
    void loggedOutUserCannotStartARound() throws Exception {
        mvc.perform(post("/api/practice/attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\": 5}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(practiceAttemptService);
    }

    @Test
    void start_retriesOnce_whenTwoStartsClashOnTheTotalsRow() throws Exception {
        // First call loses the race for the SubjectStats row, the retry succeeds.
        when(practiceAttemptService.start(5L, EMAIL))
                .thenThrow(new DataIntegrityViolationException("duplicate subject_stats row"))
                .thenReturn(new PracticeStartResponse(10L, "Viltvård", List.of()));

        mvc.perform(post("/api/practice/attempts").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(10));

        verify(practiceAttemptService, times(2)).start(5L, EMAIL);
    }

    @Test
    void start_returnsRoundAndQuestions_withoutCorrectAnswers() throws Exception {
        ExamQuestionResponse question = new ExamQuestionResponse(
                100L, "Question?", "One", "Two", "Three", "Four", null);
        when(practiceAttemptService.start(5L, EMAIL))
                .thenReturn(new PracticeStartResponse(10L, "Viltvård", List.of(question)));

        mvc.perform(post("/api/practice/attempts").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value(10))
                .andExpect(jsonPath("$.subjectName").value("Viltvård"))
                .andExpect(jsonPath("$.questions[0].id").value(100))
                .andExpect(jsonPath("$.questions[0].optionB").value("Two"))
                // The whole point of the refactor: these never reach the browser in advance
                .andExpect(jsonPath("$.questions[0].correctAnswer").doesNotExist())
                .andExpect(jsonPath("$.questions[0].explanation").doesNotExist());
    }

    @Test
    void start_withoutSubjectId_returns400() throws Exception {
        mvc.perform(post("/api/practice/attempts").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // @Valid stops the request before the service is called
        verifyNoInteractions(practiceAttemptService);
    }

    @Test
    void start_unknownSubject_returns404() throws Exception {
        when(practiceAttemptService.start(99L, EMAIL)).thenThrow(new NotFoundException("Subject not found"));

        mvc.perform(post("/api/practice/attempts").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\": 99}"))
                .andExpect(status().isNotFound());
    }

    // ---------- answer ----------

    @Test
    void answer_returnsVerdictCorrectOptionAndExplanation() throws Exception {
        when(practiceAttemptService.answer(10L, 100L, "C", EMAIL))
                .thenReturn(new PracticeAnswerResponse(false, "B", "Because two."));

        mvc.perform(post("/api/practice/attempts/10/answers").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": 100, \"chosenOption\": \"C\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.correctOption").value("B"))
                .andExpect(jsonPath("$.explanation").value("Because two."));

        // The email comes from the token, never from the request body
        verify(practiceAttemptService).answer(10L, 100L, "C", EMAIL);
    }

    @Test
    void answer_withOptionOutsideAToD_returns400() throws Exception {
        mvc.perform(post("/api/practice/attempts/10/answers").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": 100, \"chosenOption\": \"E\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(practiceAttemptService);
    }

    @Test
    void answer_withoutQuestionId_returns400() throws Exception {
        mvc.perform(post("/api/practice/attempts/10/answers").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chosenOption\": \"A\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(practiceAttemptService);
    }

    @Test
    void answer_someoneElsesRound_returns403() throws Exception {
        when(practiceAttemptService.answer(any(), any(), any(), any()))
                .thenThrow(new AccessDeniedException("You do not own this practice round."));

        mvc.perform(post("/api/practice/attempts/10/answers").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": 100, \"chosenOption\": \"A\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void answer_alreadyAnswered_returns409() throws Exception {
        when(practiceAttemptService.answer(any(), any(), any(), any()))
                .thenThrow(new ConflictException("This question has already been answered."));

        mvc.perform(post("/api/practice/attempts/10/answers").with(student())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": 100, \"chosenOption\": \"A\"}"))
                .andExpect(status().isConflict());
    }

    // ---------- finish ----------

    @Test
    void finish_returnsServerCountedTotals() throws Exception {
        when(practiceAttemptService.finish(10L, EMAIL)).thenReturn(new PracticeFinishResponse(2, 3, 4));

        mvc.perform(post("/api/practice/attempts/10/finish").with(student()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctAnswers").value(2))
                .andExpect(jsonPath("$.answeredQuestions").value(3))
                .andExpect(jsonPath("$.totalQuestions").value(4));
    }

    @Test
    void loggedOutUserCannotFinishARound() throws Exception {
        mvc.perform(post("/api/practice/attempts/10/finish"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(practiceAttemptService);
    }
}
