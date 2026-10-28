package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.entity.Question;
import com.hunterexam.backend.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminQuestionController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class AdminQuestionControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean QuestionService service;
    @MockitoBean OAuth2LoginSuccessHandler loginHandler;

    private static final String BODY = """
            {"questionText":"Which option?","optionA":"One","optionB":"Two",
             "optionC":"Three","optionD":"Four","correctAnswer":"One","subjectId":1}
            """;

    @Test
    void anonymousAndRegularUsersCannotManageQuestions() throws Exception {
        for (var request : java.util.List.of(get("/api/admin/questions?subjectId=1"),
                post("/api/admin/questions"),
                put("/api/admin/questions/1"), delete("/api/admin/questions/1"))) {
            mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(BODY))
                    .andExpect(status().isUnauthorized());
            mvc.perform(request.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(service);
    }

    @Test
    void adminCanCreateUpdateAndDelete() throws Exception {
        Question question = new Question();
        question.setId(7L);
        when(service.create(any())).thenReturn(question);
        when(service.update(eq(7L), any())).thenReturn(question);
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        mvc.perform(post("/api/admin/questions").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/admin/questions/7"))
                .andExpect(jsonPath("$.id").value(7));
        mvc.perform(put("/api/admin/questions/7").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/admin/questions/7").with(admin))
                .andExpect(status().isNoContent());
        verify(service).delete(7L);
    }

    @Test
    void adminGetsQuestionsWithCorrectAnswer() throws Exception {
        Question question = new Question();
        question.setId(7L);
        question.setCorrectAnswer("One");
        when(service.findBySubjectId(1L)).thenReturn(java.util.List.of(question));
        mvc.perform(get("/api/admin/questions?subjectId=1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].correctAnswer").value("One"));
    }

    @Test
    void rejectsInvalidInputBeforeCallingService() throws Exception {
        mvc.perform(post("/api/admin/questions")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
