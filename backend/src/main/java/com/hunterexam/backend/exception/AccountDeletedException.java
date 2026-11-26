package com.hunterexam.backend.exception;

/**
 * Raised by the JWT converter when a valid token refers to a user that no
 * longer exists (#243). Distinct from "session expired": the user didn't
 * forget to stay active, their account was removed.
 */
public class AccountDeletedException extends RuntimeException {

    public static final String CODE = "ACCOUNT_DELETED";

    public AccountDeletedException() {
        super("This account has been deleted.");
    }
}
