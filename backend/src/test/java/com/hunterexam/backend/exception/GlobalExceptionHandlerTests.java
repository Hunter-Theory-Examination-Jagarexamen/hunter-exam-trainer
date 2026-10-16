package com.hunterexam.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Calls the handler methods directly (no running server) and checks
 * that each exception type gets the right status and message.
 */
class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void invalidInputIs400WithItsMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Subject id is required."));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Subject id is required.", response.getBody().get("message"));
    }

    @Test
    void failedLoginIs401WithAGenericMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleBadCredentials(new BadCredentialsException("anything"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().get("message"));
    }

    @Test
    void accessDeniedIs403() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDenied(new AccessDeniedException("Access Denied"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void notFoundIs404WithItsMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(new NotFoundException("Question not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Question not found", response.getBody().get("message"));
    }

    @Test
    void conflictIs409WithItsMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleConflict(new ConflictException("Email is already registered"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email is already registered", response.getBody().get("message"));
    }

    @Test
    void databaseRefusalIs409WithoutDatabaseDetails() {
        ResponseEntity<Map<String, String>> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("duplicate key value violates unique constraint \"uk_subject_name\""));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("This change conflicts with existing data", response.getBody().get("message"));
    }

    @Test
    void aMessageContainingPasswordNoLongerBecomes401() {
        // Before #98 the handler searched the text for "password" and answered 401.
        ResponseEntity<Map<String, String>> response =
                handler.handleUnexpected(new RuntimeException("Cannot invoke getPassword() because user is null"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void unexpectedErrorsAre500WithoutInternalDetails() {
        ResponseEntity<Map<String, String>> response = handler.handleUnexpected(
                new RuntimeException("could not execute statement; constraint [fk_question_subject]"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        String message = response.getBody().get("message");
        // e.g. "An unexpected error occurred and has been logged as code 7F3A2C. Please try again. ..."
        assertTrue(message.matches("An unexpected error occurred and has been logged as code [0-9A-F]{6}\\. .*"));
        assertFalse(message.contains("fk_question_subject"));
    }

    @Test
    void springStatusExceptionsKeepTheirStatus() {
        ResponseEntity<Map<String, String>> response = handler.handleUnexpected(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "internal detail"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not Found", response.getBody().get("message"));
    }
}
