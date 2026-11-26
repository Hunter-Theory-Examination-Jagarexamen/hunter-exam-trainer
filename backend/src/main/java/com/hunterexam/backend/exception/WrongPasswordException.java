package com.hunterexam.backend.exception;

/**
 * A password check failed during account deletion (#243). Carries a
 * machine-readable {@code code} so the frontend can show a translated
 * message instead of the English one.
 */
public class WrongPasswordException extends RuntimeException {

    public static final String CODE = "WRONG_PASSWORD";

    public WrongPasswordException() {
        super("Wrong password");
    }
}
