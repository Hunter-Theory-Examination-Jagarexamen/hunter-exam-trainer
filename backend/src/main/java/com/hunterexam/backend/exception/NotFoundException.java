package com.hunterexam.backend.exception;

/**
 * Thrown when something the request refers to does not exist,
 * e.g. a user, subject, question or exam session.
 * GlobalExceptionHandler turns it into HTTP 404 Not Found.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
