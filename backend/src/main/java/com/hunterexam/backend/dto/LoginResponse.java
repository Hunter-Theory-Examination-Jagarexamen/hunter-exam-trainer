package com.hunterexam.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response returned after a successful login.
 * <p>
 * Contains the JWT token and the authentication type used
 * for subsequent authenticated requests.
 */
@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType;
}
