package com.eduardo.rpg.Character.DTO;

import java.time.LocalDateTime;

import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.enums.Gender;

public record CharacterResponseDTO(
    Long id,
    String name,
    Long raceId,
    String raceName,
    String raceDescription,
    Integer strengthBonus,
    Integer dexterityBonus,
    Integer constitutionBonus,
    Integer intelligenceBonus,
    Integer wisdomBonus,
    Integer charismaBonus,
    Long classId,
    String className,
    String classDescription,
    Integer classStrengthBonus,
    Integer classDexterityBonus,
    Integer classConstitutionBonus,
    Integer classIntelligenceBonus,
    Integer classWisdomBonus,
    Integer classCharismaBonus,
    CharacterRole role,
    Gender gender,
    Integer level,
    Integer experience,
    String description,
    Long userId,
    Long campaignId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

