package com.hunterexam.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    /** How long a token is valid: 1 hour. */
    private static final long TOKEN_LIFETIME_MILLIS = 1000L * 60 * 60;

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
}
