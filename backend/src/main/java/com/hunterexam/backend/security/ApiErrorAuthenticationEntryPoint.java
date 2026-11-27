package com.hunterexam.backend.security;

import com.hunterexam.backend.exception.AccountDeletedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Returns JSON in the same shape as {@code GlobalExceptionHandler} when the
 * filter chain rejects a token because the user was deleted (#243).
 * <p>
 * Registered as a bean from {@code SecurityConfig} (not a {@code @Component}),
 * so {@code @WebMvcTest} slices that include {@code SecurityConfig} still get
 * it without having to list every security bean.
 */
public class ApiErrorAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
        throws IOException {

        Throwable cause = authException.getCause();

        if (cause instanceof AccountDeletedException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                "{\"code\":\"ACCOUNT_DELETED\",\"message\":\"This account has been deleted.\"}");
            return;
        }

        // Default behavior for every other case: standard 401 with
        // WWW-Authenticate: Bearer.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
    }
}
