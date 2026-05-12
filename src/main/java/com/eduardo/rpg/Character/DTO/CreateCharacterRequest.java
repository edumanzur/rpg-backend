package com.eduardo.rpg.Character.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public record CreateCharacterRequest(

    @NotBlank(message = "Character name is required")
    String name,

    @NotBlank(message = "Race is required")
    String race,

    @NotBlank(message = "Class is required")
    String classCharacter,

    @Min(value = 1, message = "Level must be at least 1")
    Integer level,

    String description
) {}

