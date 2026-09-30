package com.hunterexam.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles illegal argument exceptions,such as duplicate emails during registration
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
    Map<String, String> response = new HashMap<>();

    if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("email") && ex.getMessage().toLowerCase().contains("already")) {
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
}

    // Handles custom runtime exceptions, such as duplicate email or user already exists
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
    Map<String, String> response = new HashMap<>();

    //Checks if message indicates a duplicate user/email conflict
    if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("already") && (ex.getMessage().toLowerCase().contains("email") || ex.getMessage().toLowerCase().contains("exists"))) {
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    // Checks if message indicates failed authentication or bad credentials
    if (ex.getMessage() != null && (ex.getMessage().toLowerCase().contains("invalid") || ex.getMessage().toLowerCase().contains("password"))) {
        response.put("message", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    response.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
}

    // Handles bad credentials exception during login attempts
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Handles validation failures on request bodies annotated with @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> response = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            response.put("message", error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // Catch-all handler for unexpected runtime exceptions to prevent Spring Security 401 overrides
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralExceptions(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
