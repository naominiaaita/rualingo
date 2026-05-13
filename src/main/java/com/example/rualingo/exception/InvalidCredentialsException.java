package com.example.rualingo.exception;

/**
 * Exception thrown when login credentials are invalid.
 * HTTP Status: 401 Unauthorized
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
