package com.hunterexam.backend.entity;

/**
 * The kind of round an {@link Attempt} is.
 * <p>
 * SUBJECT_PRACTICE is one subject, RANDOM_PRACTICE (#151) mixes subjects, and
 * MOCK_EXAM is a timed exam. They are stored as text ("SUBJECT_PRACTICE"), so
 * reordering the enum can't change old rows.
 */
public enum AttemptType {
    SUBJECT_PRACTICE,
    RANDOM_PRACTICE,
    MOCK_EXAM
}
