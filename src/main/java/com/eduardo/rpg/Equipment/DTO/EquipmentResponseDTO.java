package com.eduardo.rpg.Equipment.DTO;

import com.eduardo.rpg.enums.EquipmentType;

import java.time.LocalDateTime;
import java.util.List;

public record EquipmentResponseDTO(
    Long id,
    String name,
    String description,
    EquipmentType type,
    String damage,
    Integer strengthBonus,
    Integer dexterityBonus,
    Integer constitutionBonus,
    Integer intelligenceBonus,
    Integer wisdomBonus,
    Integer charismaBonus,
    Long campaignId,
    List<RequirementDTO> requirements,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record RequirementDTO(
        Long id,
        Long requiredClassId,
        String requiredClassName,
        Integer minStrength,
        Integer minDexterity,
        Integer minConstitution,
        Integer minIntelligence,
        Integer minWisdom,
        Integer minCharisma
    ) {}
}

