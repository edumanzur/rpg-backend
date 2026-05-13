package com.eduardo.rpg.Character.DTO;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.enums.Gender;
import org.springframework.stereotype.Component;

@Component
public class CharacterMapper {

    public CharacterResponseDTO toResponse(Character character) {
        if (character == null) return null;

        return new CharacterResponseDTO(
            character.getId(),
            character.getName(),
            character.getRace() != null ? character.getRace().getId() : null,
            character.getRace() != null ? character.getRace().getName() : null,
            character.getRace() != null ? character.getRace().getDescription() : null,
            character.getRace() != null ? character.getRace().getStrengthBonus() : null,
            character.getRace() != null ? character.getRace().getDexterityBonus() : null,
            character.getRace() != null ? character.getRace().getConstitutionBonus() : null,
            character.getRace() != null ? character.getRace().getIntelligenceBonus() : null,
            character.getRace() != null ? character.getRace().getWisdomBonus() : null,
            character.getRace() != null ? character.getRace().getCharismaBonus() : null,
            character.getCharacterClass() != null ? character.getCharacterClass().getId() : null,
            character.getCharacterClass() != null ? character.getCharacterClass().getName() : null,
            character.getCharacterClass() != null ? character.getCharacterClass().getDescription() : null,
            character.getCharacterClass() != null ? character.getCharacterClass().getStrengthBonus() : null,
            character.getCharacterClass() != null ? character.getCharacterClass().getDexterityBonus() : null,
            character.getCharacterClass() != null ? character.getCharacterClass().getConstitutionBonus() : null,
            character.getCharacterClass() != null ? character.getCharacterClass().getIntelligenceBonus() : null,
            character.getCharacterClass() != null ? character.getCharacterClass().getWisdomBonus() : null,
            character.getCharacterClass() != null ? character.getCharacterClass().getCharismaBonus() : null,
            character.getRole(),
            character.getGender(),
            character.getLevel(),
            character.getExperience(),
            character.getDescription(),
            character.getUser() != null ? character.getUser().getId() : null,
            character.getCampaign() != null ? character.getCampaign().getId() : null,
            character.getCreatedAt(),
            character.getUpdatedAt()
        );
    }

    public Character toEntity(CreateCharacterRequest dto) {
        if (dto == null) return null;

        Character character = new Character();
        character.setName(dto.name());
        character.setRole(dto.role() != null ? dto.role() : com.eduardo.rpg.enums.CharacterRole.PLAYER);
        character.setGender(dto.gender() != null ? dto.gender() : Gender.UNSPECIFIED);
        character.setLevel(dto.level() != null ? dto.level() : 1);
        character.setExperience(0);
        character.setDescription(dto.description());

        return character;
    }

    public Character toEntity(UpdateCharacterRequest dto, Character character) {
        if (dto == null) return character;

        character.setName(dto.name());
        character.setRole(dto.role());
        character.setGender(dto.gender());
        character.setLevel(dto.level());
        character.setExperience(dto.experience());
        character.setDescription(dto.description());

        return character;
    }
}

