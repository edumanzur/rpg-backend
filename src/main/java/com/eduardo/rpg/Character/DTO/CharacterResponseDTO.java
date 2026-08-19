package com.eduardo.rpg.Character.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.enums.Gender;
import com.eduardo.rpg.Equipment.DTO.EquipmentResponseDTO;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellResponseDTO;

public record CharacterResponseDTO(
    Long id,
    String name,
    RaceDTO race,
    ClassDTO characterClass,
    CharacterRole role,
    Gender gender,
    Integer level,
    Integer experience,
    String description,
    Long userId,
    Long campaignId,
    List<EquipmentResponseDTO> equipments,
    List<AbilitySpellResponseDTO> abilities,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record RaceDTO(
        Long id,
        String name,
        String description,
        Integer strengthBonus,
        Integer dexterityBonus,
        Integer constitutionBonus,
        Integer intelligenceBonus,
        Integer wisdomBonus,
        Integer charismaBonus
    ) {}

    public record ClassDTO(
        Long id,
        String name,
        String description,
        Integer strengthBonus,
        Integer dexterityBonus,
        Integer constitutionBonus,
        Integer intelligenceBonus,
        Integer wisdomBonus,
        Integer charismaBonus
    ) {}
}


