package com.eduardo.rpg.Character.DTO;

import java.time.LocalDateTime;

public record CharacterResponseDTO(
    Long id,
    String name,
    String race,
    String classCharacter,
    Integer level,
    Integer experience,
    String description,
    Long userId,
    Long campaignId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

