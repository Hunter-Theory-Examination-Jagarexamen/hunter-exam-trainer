package com.hunterexam.backend.controller;

import com.hunterexam.backend.dto.LoginRequest;
import com.hunterexam.backend.dto.LoginResponse;
import com.hunterexam.backend.dto.RegisterRequest;
import com.hunterexam.backend.dto.RegisterResponse;
import com.hunterexam.backend.entity.User;
import com.hunterexam.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user authentication.
 * <p>
 * Provides endpoints for registering new users and logging in.
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    /**
     * Registers a new user.
     * <p>
     * The request is validated before being passed to the AuthService.
     * A successful registration returns the created user's basic information.
     *
     * @param request registration details provided by the client
     * @return the registered user's information with HTTP 201 Created
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /**
     * Authenticates an existing user.
     * <p>
     * The AuthService validates the user's credentials and generates
     * a JWT token when authentication is successful.
     *
     * @param request login credentials provided by the client
     * @return JWT token with the Bearer authentication type
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        String token = authService.login(request);

        LoginResponse response = new LoginResponse(token, "Bearer");

        return ResponseEntity.ok(response);
    }


}
