package com.example.rualingo.exception;

/**
 * Exception thrown when attempting to register or create a user with an email that already exists.
 * HTTP Status: 409 Conflict
 */
public class EmailAlreadyExistsException extends RuntimeException {
    private final String email;

    public EmailAlreadyExistsException(String email) {
        super(String.format("User with email '%s' already exists", email));
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
