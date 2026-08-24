package com.eduardo.rpg.Grid.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record CombatGridResponseDTO(
    Long id,
    Long sessionId,
    Integer rows,
    Integer cols,
    List<GridTokenResponseDTO> tokens,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
