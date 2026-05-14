package com.eduardo.rpg.Race.DTO;

import java.time.LocalDateTime;

public record RaceResponseDTO(
    Long id,
    String name,
    String description,
    Integer strengthBonus,
    Integer dexterityBonus,
    Integer constitutionBonus,
    Integer intelligenceBonus,
    Integer wisdomBonus,
    Integer charismaBonus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

