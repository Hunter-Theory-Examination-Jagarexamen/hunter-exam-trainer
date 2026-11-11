package com.hunterexam.backend.controller;

import com.hunterexam.backend.config.OAuth2LoginSuccessHandler;
import com.hunterexam.backend.config.SecurityConfig;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Shared setup for the {@code @WebMvcTest} controller tests (#216).
 *
 * <p>The tests load our real {@link SecurityConfig}, so they check the real
 * access rules (e.g. a student gets 403 on admin endpoints). A {@code @WebMvcTest}
 * only starts the web layer, without services or repositories, so everything
 * {@code SecurityConfig} needs is mocked here, once. When {@code SecurityConfig}
 * gets a new dependency, add its mock here instead of in every test class.
 *
 * <p>Each test class extends this class, adds {@code @WebMvcTest(SomeController.class)}
 * and mocks only the services its own controller uses.
 */
@ActiveProfiles("test")
@Import(SecurityConfig.class)
abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mvc;

    @MockitoBean
    protected OAuth2LoginSuccessHandler loginHandler;

    /**
     * Needed by {@code UserExistsJwtAuthenticationConverter} (#215), which
     * {@code SecurityConfig} uses to reject tokens of deleted users. The tests
     * log in with {@code jwt()} from spring-security-test, which puts the user
     * straight into the security context without that converter, so this mock
     * only has to exist; no stubbing needed.
     */
    @MockitoBean
    protected UserRepository userRepository;
}
