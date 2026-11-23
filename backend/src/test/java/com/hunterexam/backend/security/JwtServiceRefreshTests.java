package com.hunterexam.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceRefreshTests {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Same test secret the other tests use.
        ReflectionTestUtils.setField(jwtService, "secret",
            "0123456789012345678901234567890123456789012345678901234567890123");
    }

    private Jwt tokenIssuedSecondsAgo(long seconds) {
        Instant iat = Instant.now().minusSeconds(seconds);
        return Jwt.withTokenValue("dummy")
            .header("alg", "HS256")
            .subject("student@example.test")
            .claim("role", "STUDENT")
            .issuedAt(iat)
            .expiresAt(iat.plusSeconds(JwtService.TOKEN_LIFETIME_MILLIS / 1000))
            .build();
    }

    @Test
    void freshTokenIsNotRefreshed() {
        Optional<String> refreshed = jwtService.refreshIfStale(tokenIssuedSecondsAgo(5 * 60));
        assertThat(refreshed).isEmpty();
    }

    @Test
    void staleTokenIsRefreshed() {
        Optional<String> refreshed = jwtService.refreshIfStale(tokenIssuedSecondsAgo(40 * 60));
        assertThat(refreshed).isPresent();
        assertThat(refreshed.get()).isNotBlank();
    }

    @Test
    void tokenWithoutIatIsNotRefreshed() {
        Jwt noIat = Jwt.withTokenValue("dummy")
            .header("alg", "HS256")
            .subject("student@example.test")
            .claim("role", "STUDENT")
            .build();
        assertThat(jwtService.refreshIfStale(noIat)).isEmpty();
    }
}
