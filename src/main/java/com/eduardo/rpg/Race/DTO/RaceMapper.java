package com.eduardo.rpg.Race.DTO;

import com.eduardo.rpg.Race.Race;
import org.springframework.stereotype.Component;

@Component
public class RaceMapper {

    public RaceResponseDTO toResponse(Race race) {
        if (race == null) {
            return null;
        }

        return new RaceResponseDTO(
            race.getId(),
            race.getName(),
            race.getDescription(),
            race.getStrengthBonus(),
            race.getDexterityBonus(),
            race.getConstitutionBonus(),
            race.getIntelligenceBonus(),
            race.getWisdomBonus(),
            race.getCharismaBonus(),
            race.getSkillBonusName(),
            race.getSkillBonusValue(),
            race.getCampaignId(),
            race.getCreatedAt(),
            race.getUpdatedAt()
        );
    }

    public Race toEntity(CreateRaceRequest dto) {
        if (dto == null) {
            return null;
        }

        Race race = new Race();
        apply(dto.name(), dto.description(), dto.strengthBonus(), dto.dexterityBonus(), dto.constitutionBonus(), dto.intelligenceBonus(), dto.wisdomBonus(), dto.charismaBonus(), dto.skillBonusName(), dto.skillBonusValue(), race);
        race.setCampaignId(dto.campaignId());
        return race;
    }

    public Race toEntity(UpdateRaceRequest dto, Race race) {
        if (dto == null || race == null) {
            return race;
        }

        apply(dto.name(), dto.description(), dto.strengthBonus(), dto.dexterityBonus(), dto.constitutionBonus(), dto.intelligenceBonus(), dto.wisdomBonus(), dto.charismaBonus(), dto.skillBonusName(), dto.skillBonusValue(), race);
        return race;
    }

    private void apply(String name, String description, Integer strengthBonus, Integer dexterityBonus, Integer constitutionBonus, Integer intelligenceBonus, Integer wisdomBonus, Integer charismaBonus, String skillBonusName, Integer skillBonusValue, Race race) {
        race.setName(name);
        race.setDescription(description);
        race.setStrengthBonus(normalizeBonus(strengthBonus));
        race.setDexterityBonus(normalizeBonus(dexterityBonus));
        race.setConstitutionBonus(normalizeBonus(constitutionBonus));
        race.setIntelligenceBonus(normalizeBonus(intelligenceBonus));
        race.setWisdomBonus(normalizeBonus(wisdomBonus));
        race.setCharismaBonus(normalizeBonus(charismaBonus));
        race.setSkillBonusName(skillBonusName != null && skillBonusName.isBlank() ? null : skillBonusName);
        race.setSkillBonusValue(normalizeBonus(skillBonusValue));
    }

    private Integer normalizeBonus(Integer value) {
        return value != null ? value : 0;
    }
}

