package com.eduardo.rpg.CharacterClass.DTO;

import com.eduardo.rpg.CharacterClass.CharacterClass;
import org.springframework.stereotype.Component;

@Component
public class CharacterClassMapper {

    public CharacterClassResponseDTO toResponse(CharacterClass characterClass) {
        if (characterClass == null) {
            return null;
        }

        return new CharacterClassResponseDTO(
            characterClass.getId(),
            characterClass.getName(),
            characterClass.getDescription(),
            characterClass.getStrengthBonus(),
            characterClass.getDexterityBonus(),
            characterClass.getConstitutionBonus(),
            characterClass.getIntelligenceBonus(),
            characterClass.getWisdomBonus(),
            characterClass.getCharismaBonus(),
            characterClass.getCreatedAt(),
            characterClass.getUpdatedAt()
        );
    }

    public CharacterClass toEntity(CreateCharacterClassRequest dto) {
        if (dto == null) {
            return null;
        }

        CharacterClass characterClass = new CharacterClass();
        apply(dto.name(), dto.description(), dto.strengthBonus(), dto.dexterityBonus(), dto.constitutionBonus(), dto.intelligenceBonus(), dto.wisdomBonus(), dto.charismaBonus(), characterClass);
        return characterClass;
    }

    public CharacterClass toEntity(UpdateCharacterClassRequest dto, CharacterClass characterClass) {
        if (dto == null || characterClass == null) {
            return characterClass;
        }

        apply(dto.name(), dto.description(), dto.strengthBonus(), dto.dexterityBonus(), dto.constitutionBonus(), dto.intelligenceBonus(), dto.wisdomBonus(), dto.charismaBonus(), characterClass);
        return characterClass;
    }

    private void apply(String name, String description, Integer strengthBonus, Integer dexterityBonus, Integer constitutionBonus, Integer intelligenceBonus, Integer wisdomBonus, Integer charismaBonus, CharacterClass characterClass) {
        characterClass.setName(name);
        characterClass.setDescription(description);
        characterClass.setStrengthBonus(normalizeBonus(strengthBonus));
        characterClass.setDexterityBonus(normalizeBonus(dexterityBonus));
        characterClass.setConstitutionBonus(normalizeBonus(constitutionBonus));
        characterClass.setIntelligenceBonus(normalizeBonus(intelligenceBonus));
        characterClass.setWisdomBonus(normalizeBonus(wisdomBonus));
        characterClass.setCharismaBonus(normalizeBonus(charismaBonus));
    }

    private Integer normalizeBonus(Integer value) {
        return value != null ? value : 0;
    }
}

