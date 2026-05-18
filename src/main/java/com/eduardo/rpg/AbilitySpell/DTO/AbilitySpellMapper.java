package com.eduardo.rpg.AbilitySpell.DTO;

import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import com.eduardo.rpg.AbilitySpell.Requirement.AbilityRequirement;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AbilitySpellMapper {

    public AbilitySpellResponseDTO toResponse(AbilitySpell abilitySpell) {
        if (abilitySpell == null) {
            return null;
        }

        List<AbilitySpellResponseDTO.RequirementDTO> requirements = abilitySpell.getRequirements() == null
            ? Collections.emptyList()
            : abilitySpell.getRequirements().stream()
                .map(this::toRequirementResponse)
                .toList();

        return new AbilitySpellResponseDTO(
            abilitySpell.getId(),
            abilitySpell.getName(),
            abilitySpell.getDamage(),
            abilitySpell.getEffect(),
            abilitySpell.getMainStatus(),
            abilitySpell.getDescription(),
            abilitySpell.getCost(),
            abilitySpell.getCostType(),
            abilitySpell.getRequiredLevel(),
            requirements,
            abilitySpell.getCreatedAt(),
            abilitySpell.getUpdatedAt()
        );
    }

    public AbilitySpell toEntity(AbilitySpellRequest dto) {
        if (dto == null) {
            return null;
        }

        AbilitySpell abilitySpell = new AbilitySpell();
        apply(dto.name(), dto.damage(), dto.effect(), dto.mainStatus(), dto.description(), dto.cost(), dto.costType(), dto.requiredLevel(), abilitySpell);
        return abilitySpell;
    }

    public AbilitySpell toEntity(AbilitySpellRequest dto, AbilitySpell abilitySpell) {
        if (dto == null || abilitySpell == null) {
            return abilitySpell;
        }

        apply(dto.name(), dto.damage(), dto.effect(), dto.mainStatus(), dto.description(), dto.cost(), dto.costType(), dto.requiredLevel(), abilitySpell);
        return abilitySpell;
    }

    public AbilitySpellResponseDTO.RequirementDTO toRequirementResponse(AbilityRequirement requirement) {
        if (requirement == null) {
            return null;
        }

        return new AbilitySpellResponseDTO.RequirementDTO(
            requirement.getId(),
            requirement.getRequiredClass() != null ? requirement.getRequiredClass().getId() : null,
            requirement.getRequiredClass() != null ? requirement.getRequiredClass().getName() : null,
            requirement.getMinLevel(),
            requirement.getMinStrength(),
            requirement.getMinDexterity(),
            requirement.getMinConstitution(),
            requirement.getMinIntelligence(),
            requirement.getMinWisdom(),
            requirement.getMinCharisma()
        );
    }

    private void apply(String name, String damage, String effect, String mainStatus, String description, String cost, com.eduardo.rpg.enums.CostType costType, Integer requiredLevel, AbilitySpell abilitySpell) {
        abilitySpell.setName(name);
        abilitySpell.setDamage(damage);
        abilitySpell.setEffect(effect);
        abilitySpell.setMainStatus(mainStatus);
        abilitySpell.setDescription(description);
        abilitySpell.setCost(cost);
        abilitySpell.setCostType(costType);
        abilitySpell.setRequiredLevel(normalize(requiredLevel));
    }

    private Integer normalize(Integer value) {
        return value != null ? value : 0;
    }
}

