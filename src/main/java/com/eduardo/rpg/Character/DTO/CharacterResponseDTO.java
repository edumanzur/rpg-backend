package com.eduardo.rpg.Character.DTO;

import java.time.LocalDateTime;

import com.eduardo.rpg.enums.CharacterRole;

public record CharacterResponseDTO(
    Long id,
    String name,
    String race,
    String classCharacter,
    CharacterRole role,
    Integer level,
    Integer experience,
    String description,
    Long userId,
    Long campaignId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

