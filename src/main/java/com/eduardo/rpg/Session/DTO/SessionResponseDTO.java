package com.eduardo.rpg.Session.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record SessionResponseDTO(
    Long id,
    String title,
    String story,
    String notes,
    Long campaignId,
    List<Long> characterIds,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

