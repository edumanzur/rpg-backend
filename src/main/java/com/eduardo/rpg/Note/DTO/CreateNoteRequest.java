package com.eduardo.rpg.Note.DTO;

import jakarta.validation.constraints.NotBlank;

public record CreateNoteRequest(

    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Content is required")
    String content,

    Long sessionId
) {}
