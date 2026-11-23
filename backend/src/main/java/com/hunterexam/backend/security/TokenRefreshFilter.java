package com.hunterexam.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds {@value #REFRESH_HEADER} to the response when the request's JWT
 * was old enough that a refresh is due (#210).
 * <p>
 * Runs after the bearer-token filter, so it only sees authenticated requests
 * with a valid JWT. Never changes the status code: the current request
 * already succeeded with the old token, and the new one is picked up by the
 * client from the header.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class TokenRefreshFilter extends OncePerRequestFilter {

    /** Response header the frontend reads to replace its stored token. */
    public static final String REFRESH_HEADER = "X-Refreshed-Token";

    private final JwtService jwtService;

    public TokenRefreshFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            jwtService.refreshIfStale(jwtAuth.getToken())
                .ifPresent(newToken -> response.setHeader(REFRESH_HEADER, newToken));
        }

        filterChain.doFilter(request, response);
    }
}
