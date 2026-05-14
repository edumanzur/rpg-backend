package com.eduardo.rpg.Equipment.DTO;

import com.eduardo.rpg.enums.EquipmentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateEquipmentRequest(
    @NotBlank(message = "Equipment name is required")
    String name,
    String description,
    @NotNull(message = "Equipment type is required")
    EquipmentType type,
    String damage,
    @Min(value = 0, message = "Strength bonus cannot be negative")
    Integer strengthBonus,
    @Min(value = 0, message = "Dexterity bonus cannot be negative")
    Integer dexterityBonus,
    @Min(value = 0, message = "Constitution bonus cannot be negative")
    Integer constitutionBonus,
    @Min(value = 0, message = "Intelligence bonus cannot be negative")
    Integer intelligenceBonus,
    @Min(value = 0, message = "Wisdom bonus cannot be negative")
    Integer wisdomBonus,
    @Min(value = 0, message = "Charisma bonus cannot be negative")
    Integer charismaBonus,
    @Valid
    List<RequirementRequest> requirements
) {
    public record RequirementRequest(
        @NotNull(message = "Required class is required")
        Long requiredClassId,
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

