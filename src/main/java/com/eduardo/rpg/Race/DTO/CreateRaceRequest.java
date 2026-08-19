package com.eduardo.rpg.Race.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateRaceRequest(
    @NotBlank(message = "Race name is required")
    String name,
    String description,
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
    Long campaignId
) {}

