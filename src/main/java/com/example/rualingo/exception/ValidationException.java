package com.example.rualingo.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when request validation fails.
 * HTTP Status: 400 Bad Request
 */
public class ValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
    }

    public ValidationException(Map<String, String> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void addFieldError(String fieldName, String error) {
        this.fieldErrors.put(fieldName, error);
    }
}
