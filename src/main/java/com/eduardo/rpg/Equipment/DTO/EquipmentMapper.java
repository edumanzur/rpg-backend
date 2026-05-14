package com.eduardo.rpg.Equipment.DTO;

import com.eduardo.rpg.Equipment.Equipment;
import com.eduardo.rpg.Equipment.Requirement.EquipmentRequirement;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class EquipmentMapper {

    public EquipmentResponseDTO toResponse(Equipment equipment) {
        if (equipment == null) {
            return null;
        }

        List<EquipmentResponseDTO.RequirementDTO> requirements = equipment.getRequirements() == null
            ? Collections.emptyList()
            : equipment.getRequirements().stream()
                .map(this::toRequirementResponse)
                .toList();

        return new EquipmentResponseDTO(
            equipment.getId(),
            equipment.getName(),
            equipment.getDescription(),
            equipment.getType(),
            equipment.getDamage(),
            equipment.getStrengthBonus(),
            equipment.getDexterityBonus(),
            equipment.getConstitutionBonus(),
            equipment.getIntelligenceBonus(),
            equipment.getWisdomBonus(),
            equipment.getCharismaBonus(),
            requirements,
            equipment.getCreatedAt(),
            equipment.getUpdatedAt()
        );
    }

    public Equipment toEntity(CreateEquipmentRequest dto) {
        if (dto == null) {
            return null;
        }

        Equipment equipment = new Equipment();
        apply(dto.name(), dto.description(), dto.type(), dto.damage(), dto.strengthBonus(), dto.dexterityBonus(), dto.constitutionBonus(), dto.intelligenceBonus(), dto.wisdomBonus(), dto.charismaBonus(), equipment);
        return equipment;
    }

    public Equipment toEntity(UpdateEquipmentRequest dto, Equipment equipment) {
        if (dto == null || equipment == null) {
            return equipment;
        }

        apply(dto.name(), dto.description(), dto.type(), dto.damage(), dto.strengthBonus(), dto.dexterityBonus(), dto.constitutionBonus(), dto.intelligenceBonus(), dto.wisdomBonus(), dto.charismaBonus(), equipment);
        return equipment;
    }

    public EquipmentResponseDTO.RequirementDTO toRequirementResponse(EquipmentRequirement requirement) {
        if (requirement == null) {
            return null;
        }

        return new EquipmentResponseDTO.RequirementDTO(
            requirement.getId(),
            requirement.getRequiredClass() != null ? requirement.getRequiredClass().getId() : null,
            requirement.getRequiredClass() != null ? requirement.getRequiredClass().getName() : null,
            requirement.getMinStrength(),
            requirement.getMinDexterity(),
            requirement.getMinConstitution(),
            requirement.getMinIntelligence(),
            requirement.getMinWisdom(),
            requirement.getMinCharisma()
        );
    }

    private void apply(String name, String description, com.eduardo.rpg.enums.EquipmentType type, String damage, Integer strengthBonus, Integer dexterityBonus, Integer constitutionBonus, Integer intelligenceBonus, Integer wisdomBonus, Integer charismaBonus, Equipment equipment) {
        equipment.setName(name);
        equipment.setDescription(description);
        equipment.setType(type);
        equipment.setDamage(damage);
        equipment.setStrengthBonus(normalizeBonus(strengthBonus));
        equipment.setDexterityBonus(normalizeBonus(dexterityBonus));
        equipment.setConstitutionBonus(normalizeBonus(constitutionBonus));
        equipment.setIntelligenceBonus(normalizeBonus(intelligenceBonus));
        equipment.setWisdomBonus(normalizeBonus(wisdomBonus));
        equipment.setCharismaBonus(normalizeBonus(charismaBonus));
    }

    private Integer normalizeBonus(Integer value) {
        return value != null ? value : 0;
    }
}

