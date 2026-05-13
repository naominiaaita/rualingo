package com.example.rualingo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all REST endpoints.
 * Provides consistent error response format across the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException - 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle EmailAlreadyExistsException - 409 Conflict
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "EMAIL_ALREADY_EXISTS",
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle DuplicateUsernameException - 409 Conflict
     */
    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(
            DuplicateUsernameException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "DUPLICATE_USERNAME",
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle InvalidCredentialsException - 401 Unauthorized
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_CREDENTIALS",
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle ValidationException - 400 Bad Request with field errors
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_FAILED",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getFieldErrors()
        );
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle MethodArgumentNotValidException - 400 Bad Request (Spring validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_FAILED",
                "Request validation failed",
                HttpStatus.BAD_REQUEST.value(),
                fieldErrors
        );
        errorResponse.setPath(request.getRequestURI());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle UnauthorizedException - 403 Forbidden
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "UNAUTHORIZED_ACCESS",
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle AccessDeniedException - 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "ACCESS_DENIED",
                "You do not have permission to access this resource",
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle CourseEnrollmentException - 400 Bad Request
     */
    @ExceptionHandler(CourseEnrollmentException.class)
    public ResponseEntity<ErrorResponse> handleCourseEnrollmentException(
            CourseEnrollmentException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "ENROLLMENT_FAILED",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ExerciseSubmissionException - 400 Bad Request
     */
    @ExceptionHandler(ExerciseSubmissionException.class)
    public ResponseEntity<ErrorResponse> handleExerciseSubmissionException(
            ExerciseSubmissionException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "SUBMISSION_FAILED",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException - 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_REQUEST",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic Exception - 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {
        
        // Log the exception for debugging
        ex.printStackTrace();

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
