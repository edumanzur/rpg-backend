package com.eduardo.rpg.Note.DTO;

import java.time.LocalDateTime;

public record NoteResponseDTO(
    Long id,
    String title,
    String content,
    Long campaignId,
    Long sessionId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
