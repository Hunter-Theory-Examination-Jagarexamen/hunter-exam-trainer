package com.hunterexam.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.UUID;

/**
 * Turns exceptions thrown by controllers and services into JSON error responses.
 * The HTTP status depends only on the exception TYPE, never on the message text.
 * Every response body has the same shape: {"message": "..."}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400: the request body is not valid JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableRequest(HttpMessageNotReadableException ex) {
        // Parser details can include submitted passwords or reset tokens, so they are not sent back.
        return error(HttpStatus.BAD_REQUEST, "Please provide a valid JSON request body");
    }

    // 400: a field annotated with @Valid rules (e.g. @NotBlank) failed
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Invalid request"
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return error(HttpStatus.BAD_REQUEST, message);
    }


    // 400: the password typed in the delete-account form was wrong (#243)
    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<Map<String, String>> handleWrongPassword(WrongPasswordException ex) {
        return errorWithCode(HttpStatus.BAD_REQUEST, WrongPasswordException.CODE, ex.getMessage());
    }

    // 409: the caller is the last remaining admin (#243)
    @ExceptionHandler(LastAdminException.class)
    public ResponseEntity<Map<String, String>> handleLastAdmin(LastAdminException ex) {
        return errorWithCode(HttpStatus.CONFLICT, LastAdminException.CODE, ex.getMessage());
    }

    // 401: a valid token refers to a user whose account was deleted (#243)
    @ExceptionHandler(AccountDeletedException.class)
    public ResponseEntity<Map<String, String>> handleAccountDeleted(AccountDeletedException ex) {
        return errorWithCode(HttpStatus.UNAUTHORIZED, AccountDeletedException.CODE, ex.getMessage());
    }

    // 400: invalid input, e.g. "Correct answer must match one of the option texts"
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400: a URL value of the wrong type, e.g. ?type=NONSENSE or ?subjectId=abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return error(HttpStatus.BAD_REQUEST, "Invalid value for '" + ex.getName() + "'");
    }

    // 401: failed login. Same message for unknown email and wrong password.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    // 403: logged in, but not allowed (e.g. a student calling an admin-only method)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "You do not have permission to do this");
    }

    // 404: what the request refers to does not exist
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 409: clashes with existing data, e.g. email already registered
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 409: the database refused the change, e.g. a duplicate unique value or deleting
    // a row that other rows still point to. The database text (table and constraint
    // names) goes to the log only.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Database refused a change: {}", ex.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT, "This change conflicts with existing data");
    }

    // 429: too many requests in a short time, e.g. repeated password reset emails
    // for one address. The message is fixed by the caller and never contains the
    // requested address, so it is safe to return as-is.
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, String>> handleTooManyRequests(TooManyRequestsException ex) {
        return error(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    // Everything else
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        // Spring's own web errors (unknown URL, wrong HTTP method, ResponseStatusException...)
        // already know their status, so keep it.
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            HttpStatus known = HttpStatus.resolve(status.value());
            return error(status, known != null ? known.getReasonPhrase() : "Request failed");
        }

        // A real bug or an outside failure (e.g. the database). The details go to the
        // server log only; the client gets a generic message without SQL or class names.
        // The short code is in both, so a reported code can be found in the log.
        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        log.error("Unexpected error, code {}", code, ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred and has been logged as code " + code
                        + ". Please try again. If it keeps happening, contact your course leader.");
    }

    // Builds the {"message": "..."} body that every handler returns.
    private ResponseEntity<Map<String, String>> error(HttpStatusCode status, String message) {
        String safeMessage = message != null ? message : "Request failed";
        return ResponseEntity.status(status).body(Map.of("message", safeMessage));
    }

    // For the #243 delete-account errors: body carries a code the frontend
    // can map to a translated message. Only these three cases use it for
    // now; every other handler keeps the "message only" shape so nothing
    // else in the app changes behaviour.
    private ResponseEntity<Map<String, String>> errorWithCode(
        HttpStatusCode status, String code, String message) {
        String safeMessage = message != null ? message : "Request failed";
        return ResponseEntity.status(status)
            .body(Map.of("code", code, "message", safeMessage));
    }
}

