package com.hunterexam.backend.entity;

/**
 * What a {@link Feedback} item is about. Stored as text ("QUESTION"), so
 * reordering the enum can't change old rows.
 */
public enum FeedbackType {
    /** A problem with a specific question (wrong answer, unclear wording, broken image). */
    QUESTION,
    /** Feedback about the app itself. */
    APP,
    OTHER
}
