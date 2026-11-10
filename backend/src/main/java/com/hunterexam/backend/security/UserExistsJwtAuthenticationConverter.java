package com.hunterexam.backend.security;

import com.hunterexam.backend.repository.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Validates that the user still exists and builds the authentication token.
 * <p>
 * Implements {@link Converter} directly because {@link org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter}
 * has a final {@code convert} method and cannot be subclassed for this purpose.
 */
@Component
public class UserExistsJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private static final String INVALID_TOKEN = "invalid_token";

    private final UserRepository userRepository;
    private final JwtGrantedAuthoritiesConverter authoritiesConverter;

    public UserExistsJwtAuthenticationConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
        // Build the same role mapping as the original bean.
        this.authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        this.authoritiesConverter.setAuthorityPrefix("ROLE_");
        this.authoritiesConverter.setAuthoritiesClaimName("role");
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getSubject();

        if (email == null || !userRepository.existsByEmail(email)) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error(INVALID_TOKEN, "User no longer exists", null));
        }

        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }
}
