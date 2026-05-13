package com.eduardo.rpg.Character.DTO;

import com.eduardo.rpg.Character.Character;
import org.springframework.stereotype.Component;

@Component
public class CharacterMapper {

    public CharacterResponseDTO toResponse(Character character) {
        if (character == null) return null;

        return new CharacterResponseDTO(
            character.getId(),
            character.getName(),
            character.getRace(),
            character.getClassCharacter(),
            character.getRole(),
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
        character.setRace(dto.race());
        character.setClassCharacter(dto.classCharacter());
        character.setRole(dto.role() != null ? dto.role() : com.eduardo.rpg.enums.CharacterRole.PLAYER);
        character.setLevel(dto.level() != null ? dto.level() : 1);
        character.setExperience(0);
        character.setDescription(dto.description());

        return character;
    }

    public Character toEntity(UpdateCharacterRequest dto, Character character) {
        if (dto == null) return character;

        character.setName(dto.name());
        character.setRace(dto.race());
        character.setClassCharacter(dto.classCharacter());
        character.setRole(dto.role());
        character.setLevel(dto.level());
        character.setExperience(dto.experience());
        character.setDescription(dto.description());

        return character;
    }
}

