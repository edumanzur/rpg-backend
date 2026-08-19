package com.eduardo.rpg.exception;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    // Login/credenciais inválidas (ex: senha errada) é erro do cliente, não
    // do servidor - antes caía no handler genérico e virava 500.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthentication(AuthenticationException ex) {
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(),
            "Usuário inexistente ou senha inválida",
            "UNAUTHORIZED"
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
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
        String message = mapDataIntegrityViolation(ex);

        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(),
            message,
            "CONFLICT"
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    private String mapDataIntegrityViolation(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause == null || cause.getMessage() == null) {
            return "Erro de integridade de dados: os dados fornecidos conflitam com dados existentes.";
        }

        String message = cause.getMessage().toLowerCase();

        // Mapeia mensagens de erro do banco para mensagens genéricas
        if (message.contains("duplicate") || message.contains("unique")) {
            return "Já existe um registro com esses dados. Verifique se o email, nome ou outro identificador único já foi cadastrado.";
        }

        if (message.contains("foreign key") || message.contains("fk_")) {
            return "Não é possível executar esta operação porque existem dependências vinculadas a este registro.";
        }

        if (message.contains("not null") || message.contains("null")) {
            return "Alguns dados obrigatórios estão faltando. Verifique todos os campos necessários.";
        }

        if (message.contains("check constraint") || message.contains("constraint")) {
            return "Os dados fornecidos violam as regras de validação do sistema.";
        }

        return "Erro de integridade de dados: os dados fornecidos conflitam com dados existentes.";
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

    // Corpo da requisição malformado (JSON inválido, valor de enum
    // inexistente, tipo incompatível) é erro do cliente, não do servidor.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleMalformedRequest(HttpMessageNotReadableException ex) {
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(),
            "O corpo da requisição é inválido ou está mal formatado.",
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
