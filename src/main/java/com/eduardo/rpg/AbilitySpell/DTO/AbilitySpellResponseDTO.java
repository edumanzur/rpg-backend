package com.eduardo.rpg.AbilitySpell.DTO;

import com.eduardo.rpg.enums.CostType;

import java.time.LocalDateTime;
import java.util.List;

public record AbilitySpellResponseDTO(
    Long id,
    String name,
    String damage,
    String effect,
    String mainStatus,
    String description,
    String cost,
    CostType costType,
    Integer requiredLevel,
    Long campaignId,
    List<RequirementDTO> requirements,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record RequirementDTO(
        Long id,
        Long requiredClassId,
        String requiredClassName,
        Integer minLevel,
        Integer minStrength,
        Integer minDexterity,
        Integer minConstitution,
        Integer minIntelligence,
        Integer minWisdom,
        Integer minCharisma
    ) {}
}

