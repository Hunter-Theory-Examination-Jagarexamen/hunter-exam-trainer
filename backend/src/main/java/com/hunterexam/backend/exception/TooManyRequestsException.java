package com.hunterexam.backend.exception;

/**
 * Thrown when a client sends requests faster than the configured limit,
 * e.g. repeating a password reset request for the same email address.
 * GlobalExceptionHandler turns it into HTTP 429 Too Many Requests.
 */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
