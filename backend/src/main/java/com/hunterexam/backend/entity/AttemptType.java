package com.hunterexam.backend.entity;

/**
 * The kind of round an {@link Attempt} is.
 * <p>
 * Only SUBJECT_PRACTICE is used so far. RANDOM_PRACTICE (#151) and
 * MOCK_EXAM are listed now so that later issues can use them without
 * changing the table.
 */
public enum AttemptType {
    SUBJECT_PRACTICE,
    RANDOM_PRACTICE,
    MOCK_EXAM
}
