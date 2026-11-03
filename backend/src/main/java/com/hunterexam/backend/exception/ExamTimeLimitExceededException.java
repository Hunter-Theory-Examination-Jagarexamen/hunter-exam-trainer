package com.hunterexam.backend.exception;

/**
 * Thrown when a mock exam is submitted after its time limit (plus grace period).
 * <p>
 * Extends {@link IllegalArgumentException}, so GlobalExceptionHandler still
 * turns it into HTTP 400, as before. It has its own class so that
 * {@code ExamService.submitExam} can keep its database changes (closing the
 * exam) when this is thrown, while any other error rolls them back.
 */
public class ExamTimeLimitExceededException extends IllegalArgumentException {

    public ExamTimeLimitExceededException(String message) {
        super(message);
    }
}
