package com.eduardo.rpg.Race.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateRaceRequest(
    @NotBlank(message = "Race name is required")
    String name,
    String description,
    @Min(value = -10, message = "Strength bonus cannot be less than -10")
    Integer strengthBonus,
    @Min(value = -10, message = "Dexterity bonus cannot be less than -10")
    Integer dexterityBonus,
    @Min(value = -10, message = "Constitution bonus cannot be less than -10")
    Integer constitutionBonus,
    @Min(value = -10, message = "Intelligence bonus cannot be less than -10")
    Integer intelligenceBonus,
    @Min(value = -10, message = "Wisdom bonus cannot be less than -10")
    Integer wisdomBonus,
    @Min(value = -10, message = "Charisma bonus cannot be less than -10")
    Integer charismaBonus,
    String skillBonusName,
    @Min(value = 0, message = "Skill bonus cannot be negative")
    Integer skillBonusValue,
    Long campaignId
) {}

