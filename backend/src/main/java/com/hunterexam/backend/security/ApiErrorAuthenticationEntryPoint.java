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
 * filter chain itself rejects a token (#243). Stateless, so a plain {@code new}
 * instance is safe — instantiated directly inside {@code SecurityConfig}.
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

        // Default: standard 401 with WWW-Authenticate: Bearer. Same as
        // Spring's default BearerTokenAuthenticationEntryPoint.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
    }
}
