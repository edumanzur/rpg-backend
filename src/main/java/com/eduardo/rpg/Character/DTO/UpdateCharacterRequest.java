package com.eduardo.rpg.Character.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.enums.Gender;

public record UpdateCharacterRequest(

    @NotBlank(message = "Character name is required")
    String name,

    @NotNull(message = "Race is required")
    Long raceId,

    @NotNull(message = "Class is required")
    Long classId,

    @NotNull(message = "Character role is required")
    CharacterRole role,

    @NotNull(message = "Gender is required")
    Gender gender,

    @NotNull(message = "Campaign is required")
    Long campaignId,

    @Min(value = 1, message = "Level must be at least 1")
    Integer level,

    @Min(value = 0, message = "Experience cannot be negative")
    Integer experience,

    String description
) {}

