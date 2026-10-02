package com.hunterexam.backend.config;

import com.hunterexam.backend.security.JwtService;
import com.hunterexam.backend.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Handles successful Google OAuth2 logins.
 * <p>
 * The Google user is matched to an application user, a JWT is generated,
 * and the browser is redirected back to the frontend where the token is
 * stored and the user is signed in.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String FRONTEND_REDIRECT_URL =
            "http://localhost:5173/oauth2/redirect?token=";

    private final AuthService authService;
    private final JwtService jwtService;

    public OAuth2LoginSuccessHandler(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();

        Map<String, Object> attributes = principal.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.getOrDefault(
                "name",
                attributes.get("given_name")
        );

        String token = authService.oAuthLogin(
                email,
                name == null ? "User" : name
        );

        response.sendRedirect(FRONTEND_REDIRECT_URL + token);
    }
}