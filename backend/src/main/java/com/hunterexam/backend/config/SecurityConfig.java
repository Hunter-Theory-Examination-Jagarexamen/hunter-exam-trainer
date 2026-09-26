package com.hunterexam.backend.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import java.util.List;
/**
 * Configures authentication, authorization, JWT validation,
 * password encryption, and CORS for the application.
 */
@Configuration
public class SecurityConfig {

    /**
     * Provides the password encoder used to hash user passwords.
     * <p>
     * BCrypt is used so that passwords are stored as secure hashes
     * instead of plain text.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Maps the "role" claim from a JWT into a Spring Security authority
     * of the form {@code ROLE_<role>}.
     * <p>
     * Spring Security's {@code hasRole("ADMIN")} check expects an authority
     * named {@code ROLE_ADMIN}, so the prefix is added here.
     *
     * @return JWT authentication converter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    /**
     * Configures HTTP security for the application.
     * <p>
     * Registration and login are publicly accessible because users
     * need to access these endpoints before they are authenticated.
     * <p>
     * All other endpoints require a valid JWT.
     * <p>
     * OPTIONS requests are allowed to support CORS preflight requests
     * from the frontend.
     *
     * @param http Spring Security HTTP security configuration
     * @return configured security filter chain
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // CSRF is disabled because the application uses JWT authentication.
                .csrf(csrf -> csrf.disable())

                // Enable CORS using the configuration defined below.
                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth
                        // These endpoints must be accessible without authentication.
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Allow browser CORS preflight requests.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Admin-only endpoints.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // All other endpoints require authentication.
                        .anyRequest().authenticated()
                )
                // Use JWT tokens to authenticate protected requests.
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    /*
     * JWT secret used to sign and validate authentication tokens.
     * <p>
     * The value is read from the JWT_SECRET environment variable
     * through application.properties.
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Creates the secret key used for JWT signing and validation.
     * <p>
     * The configured Base64 secret is decoded before being converted
     * into an HMAC secret key.
     *
     * @return secret key used for JWT operations
     */
    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    /**
     * Creates the JWT decoder used by Spring Security to validate
     * incoming JWT tokens.
     * <p>
     * The same secret key used to create the tokens is used to
     * validate them.
     *
     * @param secretKey configured JWT secret key
     * @return JWT decoder
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .build();
    }

    /**
     * Configures CORS for requests from the React development server.
     * <p>
     * This allows the frontend running on port 5173 to communicate
     * with the Spring Boot backend running on port 8080.
     *
     * @return CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
