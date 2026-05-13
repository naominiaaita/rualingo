package com.example.rualingo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure for all API errors.
 */
@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private int status;
    private Map<String, String> fieldErrors;

    public void setPath(String path) {
        this.path = path;
    }

    public ErrorResponse(String errorCode, String message, int status) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String errorCode, String message, int status, String path) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String errorCode, String message, int status, Map<String, String> fieldErrors) {
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.fieldErrors = fieldErrors;
        this.timestamp = LocalDateTime.now();
    }
}
