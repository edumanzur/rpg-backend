package com.eduardo.rpg.exception;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    // Este método "escuta" especificamente a sua exceção customizada
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        
        // Criamos um objeto de erro para o JSON ficar bonito
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(), 
            ex.getMessage(), 
            "CONFLICT"
        );

        // Retornamos o status 409 (Conflito)
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Você pode ter outro para erros de validação (ex: campo vazio)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidation(MethodArgumentNotValidException ex) {
        // Pega a mensagem do primeiro erro de validação encontrado
        String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
    
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(), 
            message, 
            "BAD_REQUEST"
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex) {
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(), 
            "Ocorreu um erro interno inesperado.", 
            "INTERNAL_SERVER_ERROR"
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        String message = "Este usuário ou e-mail já está cadastrado no sistema.";
        
        // Aqui você usa o seu DTO de erro (ErrorDetails ou similar)
        ErrorDetails error = new ErrorDetails(
            LocalDateTime.now(),
            message,
            "CONFLICT"
        );
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT); // Retorna 409
    }
}
