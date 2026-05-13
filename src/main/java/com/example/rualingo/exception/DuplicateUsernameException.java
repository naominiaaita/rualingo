package com.example.rualingo.exception;

/**
 * Exception thrown when attempting to register or create a user with a username that already exists.
 * HTTP Status: 409 Conflict
 */
public class DuplicateUsernameException extends RuntimeException {
    private final String username;

    public DuplicateUsernameException(String username) {
        super(String.format("Username '%s' is already taken", username));
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
