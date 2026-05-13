package com.eduardo.rpg.Campaign.DTO;

import java.time.LocalDateTime;

public record StatusTemplateResponseDTO(
    Long id,
    String name,
    String description,
    Integer defaultValue,
    Integer minValue,
    Integer maxValue,
    Long campaignId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

