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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        String token = authService.login(request);

        LoginResponse response = new LoginResponse(token, "Bearer");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("JWT authentication is working.");
    }
}
