package com.eduardo.rpg.CharacterStatus.DTO;

import java.time.LocalDateTime;

public record CharacterStatusResponseDTO(
    Long id,
    Long characterId,
    Long templateId,
    String templateName,
    String templateDescription,
    Integer defaultValue,
    Integer minValue,
    Integer maxValue,
    Integer currentValue,
    Long campaignId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

