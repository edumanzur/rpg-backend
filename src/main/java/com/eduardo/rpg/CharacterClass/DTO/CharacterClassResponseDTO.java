package com.eduardo.rpg.CharacterClass.DTO;

import java.time.LocalDateTime;

public record CharacterClassResponseDTO(
    Long id,
    String name,
    String description,
    Integer strengthBonus,
    Integer dexterityBonus,
    Integer constitutionBonus,
    Integer intelligenceBonus,
    Integer wisdomBonus,
    Integer charismaBonus,
    String skillBonusName,
    Integer skillBonusValue,
    Long campaignId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

