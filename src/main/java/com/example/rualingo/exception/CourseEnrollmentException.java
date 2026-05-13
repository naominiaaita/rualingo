package com.example.rualingo.exception;

/**
 * Exception thrown when course enrollment operations fail.
 * HTTP Status: 400 Bad Request
 */
public class CourseEnrollmentException extends RuntimeException {
    public CourseEnrollmentException(String message) {
        super(message);
    }

    public CourseEnrollmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
