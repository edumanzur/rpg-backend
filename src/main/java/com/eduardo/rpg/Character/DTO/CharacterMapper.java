package com.eduardo.rpg.Character.DTO;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.enums.Gender;
import com.eduardo.rpg.Equipment.DTO.EquipmentMapper;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CharacterMapper {

    private final EquipmentMapper equipmentMapper;
    private final AbilitySpellMapper abilitySpellMapper;

    public CharacterResponseDTO toResponse(Character character) {
        if (character == null) return null;

        CharacterResponseDTO.RaceDTO raceDTO = character.getRace() != null
            ? new CharacterResponseDTO.RaceDTO(
                character.getRace().getId(),
                character.getRace().getName(),
                character.getRace().getDescription(),
                character.getRace().getStrengthBonus(),
                character.getRace().getDexterityBonus(),
                character.getRace().getConstitutionBonus(),
                character.getRace().getIntelligenceBonus(),
                character.getRace().getWisdomBonus(),
                character.getRace().getCharismaBonus()
            ) : null;

        CharacterResponseDTO.ClassDTO classDTO = character.getCharacterClass() != null
            ? new CharacterResponseDTO.ClassDTO(
                character.getCharacterClass().getId(),
                character.getCharacterClass().getName(),
                character.getCharacterClass().getDescription(),
                character.getCharacterClass().getStrengthBonus(),
                character.getCharacterClass().getDexterityBonus(),
                character.getCharacterClass().getConstitutionBonus(),
                character.getCharacterClass().getIntelligenceBonus(),
                character.getCharacterClass().getWisdomBonus(),
                character.getCharacterClass().getCharismaBonus()
            ) : null;

        return new CharacterResponseDTO(
            character.getId(),
            character.getName(),
            raceDTO,
            classDTO,
            character.getRole(),
            character.getGender(),
            character.getLevel(),
            character.getExperience(),
            character.getDescription(),
            character.getUser() != null ? character.getUser().getId() : null,
            character.getCampaign() != null ? character.getCampaign().getId() : null,
            character.getEquipments().stream().map(equipmentMapper::toResponse).toList(),
            character.getAbilities().stream().map(abilitySpellMapper::toResponse).toList(),
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

