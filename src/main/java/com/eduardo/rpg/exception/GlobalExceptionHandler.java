package com.eduardo.rpg.exception;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Exceções customizadas (mais específicas) - primeiro
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(),
            ex.getMessage(),
            "NOT_FOUND"
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(), 
            ex.getMessage(), 
            "CONFLICT"
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(),
            ex.getMessage(),
            "CONFLICT"
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDenied(AccessDeniedException ex) {
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(),
            ex.getMessage(),
            "FORBIDDEN"
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // Exceções do Spring Data (específicas de integridade)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDetails> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null
            ? ex.getMostSpecificCause().getMessage()
            : "Este usuário ou e-mail já está cadastrado no sistema.";

        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(),
            message,
            "CONFLICT"
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Exceções do Spring MVC (validação)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(), 
            message, 
            "BAD_REQUEST"
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Exceção genérica - sempre por último
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex) {
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(), 
            ex.getMessage() != null ? ex.getMessage() : "Ocorreu um erro interno inesperado.", 
            "INTERNAL_SERVER_ERROR"
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
