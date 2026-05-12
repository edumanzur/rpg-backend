package com.eduardo.rpg.exception;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String code;

    public ErrorDetails() {
    }

    public ErrorDetails(LocalDateTime timestamp, String message, String code) {
        this.timestamp = timestamp;
        this.message = message;
        this.code = code;
    }
}
