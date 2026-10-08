package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.service.ExamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link ExamController}: the real SecurityConfig and
 * controller run, but the service behind them is a mock. Requests are sent
 * with MockMvc, a fake HTTP client, so no server or database is started.
 */
@WebMvcTest(ExamController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class ExamControllerTests {

    @Autowired MockMvc mvc;
    @MockitoBean ExamService examService;
    // Needed by SecurityConfig; not used in these tests
    @MockitoBean OAuth2LoginSuccessHandler loginHandler;

    @Test
    void loggedOutUserCannotStartAnExam() throws Exception {
        // Act + Assert: a request with no token at all
        mvc.perform(post("/api/exam/start"))
                .andExpect(status().isUnauthorized());

        // The request is stopped by security before it reaches the controller
        verifyNoInteractions(examService);
    }

    @Test
    void loggedInStudentCanStartAnExam() throws Exception {
        // Act + Assert: jwt() fakes a request with a valid token for a student,
        // so we don't need JwtService or the real secret here
        mvc.perform(post("/api/exam/start")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
                .andExpect(status().isOk());

        // Together with the test above, this pins the rule from both sides:
        // logged out is blocked, logged in gets through
    }
}
