package com.hunterexam.backend.exception;

/**
 * The last remaining admin tried to delete their own account (#243). The
 * app must always keep at least one admin, so this is a 409 with a code
 * the frontend can translate.
 */
public class LastAdminException extends RuntimeException {

    public static final String CODE = "LAST_ADMIN";

    public LastAdminException() {
        super("You are the last admin and cannot delete your account.");
    }
}
