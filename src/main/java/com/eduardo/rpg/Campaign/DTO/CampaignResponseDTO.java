package com.eduardo.rpg.Campaign.DTO;

import java.time.LocalDateTime;

public record CampaignResponseDTO(
    Long id,
    String name,
    String description,
    Boolean status,
    Long masterId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

