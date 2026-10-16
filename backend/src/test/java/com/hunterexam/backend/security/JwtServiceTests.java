package com.hunterexam.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link JwtService}. No mocks needed: the service only has
 * a secret, which we set by hand instead of letting Spring read it from
 * the properties file.
 */
class JwtServiceTests {

    // Same test-only secret as in application-test.properties
    private static final String SECRET =
            "0123456789012345678901234567890123456789012345678901234567890123";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // In the app, Spring fills the private "secret" field from @Value("${jwt.secret}")
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    }

    /** Reads a token back, the same way the server checks it: the signature must match the key. */
    private Claims readToken(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Test
    void tokenContainsEmailRoleAndExpiresAfterOneHour() {
        // Act
        String token = jwtService.generateToken("student@example.test", "STUDENT");

        // Assert: read the token back with the same secret and look inside
        Claims claims = readToken(token, SECRET);

        assertEquals("student@example.test", claims.getSubject());
        assertEquals("STUDENT", claims.get("role", String.class));

        long lifetimeMillis = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(60 * 60 * 1000, lifetimeMillis, 1000, "token should live for one hour");
    }

    @Test
    void tokenCannotBeVerifiedWithAnotherSecret() {
        // Arrange: a real token, and a secret that is NOT the server's
        String token = jwtService.generateToken("student@example.test", "STUDENT");
        String otherSecret =
                "9876543210987654321098765432109876543210987654321098765432109876";

        // Act + Assert: the signature doesn't match, so the token is refused.
        // This is what stops someone from making their own token, or editing
        // "role": "STUDENT" into "ADMIN", without knowing the secret.
        assertThrows(SignatureException.class, () -> readToken(token, otherSecret));
    }
}
