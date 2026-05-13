package com.example.rualingo.exception;

/**
 * Exception thrown when a user attempts an unauthorized action.
 * HTTP Status: 403 Forbidden
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("You do not have permission to perform this action");
    }
}
