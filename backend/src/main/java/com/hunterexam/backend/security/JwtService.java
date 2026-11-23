package com.hunterexam.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    /** How long a token is valid: 1 hour. */
    public static final long TOKEN_LIFETIME_MILLIS = 1000L * 60 * 60;

    /**
     * How old a token must be before a request triggers a refresh (#210).
     * Half the lifetime, so a student actively using the app refreshes at
     * most every 30 minutes and always has at least 30 minutes left.
     */
    private static final long REFRESH_AFTER_MILLIS = TOKEN_LIFETIME_MILLIS / 2;

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String email, String role) {

        SecretKey key = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(secret)
        );

        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + TOKEN_LIFETIME_MILLIS))
            .signWith(key, Jwts.SIG.HS256)
            .compact();
    }

    /**
     * Returns a freshly issued token when the given token is older than
     * {@link #REFRESH_AFTER_MILLIS}, or empty when the token is still fresh
     * enough that a refresh would only add churn (#210).
     * <p>
     * The role claim is copied over unchanged from the old token; the caller
     * has already confirmed (via {@code UserExistsJwtAuthenticationConverter})
     * that the user still exists, and role changes are picked up on the next
     * refresh (#204 will hook into this path).
     */
    public Optional<String> refreshIfStale(org.springframework.security.oauth2.jwt.Jwt token) {
        Instant issuedAt = token.getIssuedAt();
        if (issuedAt == null) {
            return Optional.empty();
        }
        Duration age = Duration.between(issuedAt, Instant.now());
        if (age.toMillis() < REFRESH_AFTER_MILLIS) {
            return Optional.empty();
        }
        String email = token.getSubject();
        String role = token.getClaimAsString("role");
        if (email == null || role == null) {
            return Optional.empty();
        }
        return Optional.of(generateToken(email, role));
    }
}
