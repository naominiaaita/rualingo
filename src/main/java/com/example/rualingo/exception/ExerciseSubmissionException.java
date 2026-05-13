package com.example.rualingo.exception;

/**
 * Exception thrown when exercise submission or evaluation fails.
 * HTTP Status: 400 Bad Request
 */
public class ExerciseSubmissionException extends RuntimeException {
    public ExerciseSubmissionException(String message) {
        super(message);
    }

    public ExerciseSubmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
