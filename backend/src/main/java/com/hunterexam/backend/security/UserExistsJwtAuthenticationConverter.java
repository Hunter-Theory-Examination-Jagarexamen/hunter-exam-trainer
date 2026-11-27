package com.hunterexam.backend.security;

import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.exception.AccountDeletedException;
import com.hunterexam.backend.repository.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Validates that the user still exists and builds the authentication token.
 * <p>
 * The authorities come from the role stored in the database, never from the
 * {@code role} claim of the JWT: a token stays valid for an hour, so a claim
 * would keep granting a demoted administrator access until it expires. The
 * single user lookup that already rejects tokens of deleted users therefore
 * also decides what the caller may do, effective from the next request on.
 * <p>
 * Implements {@link Converter} directly because {@link org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter}
 * has a final {@code convert} method and cannot be subclassed for this purpose.
 */
@Component
public class UserExistsJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private static final String INVALID_TOKEN = "invalid_token";

    private final UserRepository userRepository;

    public UserExistsJwtAuthenticationConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getSubject();

        Optional<User> user = email == null
            ? Optional.empty()
            : userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token", "User no longer exists", null),
                new AccountDeletedException());
        }

        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + user.get().getRole().name()));

        // Same shape as before: the Jwt stays the principal, so the token still
        // identifies the user (e.g. authentication.getName() = the subject).
        return new JwtAuthenticationToken(jwt, authorities);
    }
}
