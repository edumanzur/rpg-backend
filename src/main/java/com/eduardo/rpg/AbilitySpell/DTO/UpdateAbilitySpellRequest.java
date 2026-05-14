package com.eduardo.rpg.AbilitySpell.DTO;

import com.eduardo.rpg.enums.CostType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateAbilitySpellRequest(
    @NotBlank(message = "Ability name is required")
    String name,
    String damage,
    String effect,
    String mainStatus,
    String description,
    String cost,
    @NotNull(message = "Cost type is required")
    CostType costType,
    @Min(value = 0, message = "Required level cannot be negative")
    Integer requiredLevel,
    @Valid
    List<RequirementRequest> requirements
) {
    public record RequirementRequest(
        @NotNull(message = "Required class is required")
        Long requiredClassId,
        @Min(value = 0, message = "Minimum level cannot be negative")
        Integer minLevel,
        @Min(value = 0, message = "Minimum strength cannot be negative")
        Integer minStrength,
        @Min(value = 0, message = "Minimum dexterity cannot be negative")
        Integer minDexterity,
        @Min(value = 0, message = "Minimum constitution cannot be negative")
        Integer minConstitution,
        @Min(value = 0, message = "Minimum intelligence cannot be negative")
        Integer minIntelligence,
        @Min(value = 0, message = "Minimum wisdom cannot be negative")
        Integer minWisdom,
        @Min(value = 0, message = "Minimum charisma cannot be negative")
        Integer minCharisma
    ) {}
}

