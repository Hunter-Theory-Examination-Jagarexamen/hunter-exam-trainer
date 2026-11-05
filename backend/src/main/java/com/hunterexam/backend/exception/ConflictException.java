package com.hunterexam.backend.exception;

/**
 * Thrown when a request clashes with data that already exists,
 * e.g. registering an email that is already in use.
 * GlobalExceptionHandler turns it into HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
