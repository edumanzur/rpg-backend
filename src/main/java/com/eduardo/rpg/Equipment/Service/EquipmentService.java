package com.eduardo.rpg.Equipment.Service;

import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.Equipment.DTO.CreateEquipmentRequest;
import com.eduardo.rpg.Equipment.DTO.EquipmentMapper;
import com.eduardo.rpg.Equipment.DTO.EquipmentResponseDTO;
import com.eduardo.rpg.Equipment.DTO.UpdateEquipmentRequest;
import com.eduardo.rpg.Equipment.Equipment;
import com.eduardo.rpg.Equipment.Requirement.EquipmentRequirement;
import com.eduardo.rpg.Equipment.Repository.EquipmentRepository;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.common.RequirementMapperHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final CharacterClassRepository characterClassRepository;
    private final EquipmentMapper equipmentMapper;

    @Transactional
    public EquipmentResponseDTO createEquipment(CreateEquipmentRequest dto) {
        validateNameAvailability(dto.name(), null);
        validateWeaponDamage(dto.type(), dto.damage());

        Equipment equipment = equipmentMapper.toEntity(dto);
        equipment.setRequirements(mapRequirements(equipment, dto.requirements()));
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return equipmentMapper.toResponse(savedEquipment);
    }

    @Transactional(readOnly = true)
    public EquipmentResponseDTO findEquipmentById(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("Equipamento não encontrado!");
        }

        Equipment equipment = equipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipamento não encontrado!"));
        return equipmentMapper.toResponse(equipment);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentResponseDTO> findAllEquipments(Pageable pageable) {
        return equipmentRepository.findAll(pageable)
            .map(equipmentMapper::toResponse);
    }

    @Transactional
    public EquipmentResponseDTO updateEquipment(Long id, UpdateEquipmentRequest dto) {
        Equipment equipment = equipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipamento não encontrado!"));

        validateNameAvailability(dto.name(), id);
        validateWeaponDamage(dto.type(), dto.damage());

        equipment = equipmentMapper.toEntity(dto, equipment);
        replaceRequirements(equipment, dto.requirements());
        Equipment updatedEquipment = equipmentRepository.save(equipment);
        return equipmentMapper.toResponse(updatedEquipment);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipamento não encontrado!"));
        equipmentRepository.delete(equipment);
    }

    private void validateNameAvailability(String name, Long currentId) {
        if (currentId == null) {
            if (equipmentRepository.findByNameIgnoreCase(name).isPresent()) {
                throw new IllegalArgumentException("Já existe um equipamento com este nome");
            }
            return;
        }

        equipmentRepository.findByNameIgnoreCase(name)
            .filter(existing -> !existing.getId().equals(currentId))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Já existe um equipamento com este nome");
            });
    }

    private void validateWeaponDamage(com.eduardo.rpg.enums.EquipmentType type, String damage) {
        if (type == com.eduardo.rpg.enums.EquipmentType.WEAPON && (damage == null || damage.isBlank())) {
            throw new IllegalArgumentException("Armas precisam informar o dano");
        }
    }

    private List<EquipmentRequirement> mapRequirements(Equipment equipment, List<CreateEquipmentRequest.RequirementRequest> requests) {
        return RequirementMapperHelper.mapRequirements(
            requests,
            "equipamento",
            CreateEquipmentRequest.RequirementRequest::requiredClassId,
            classId -> characterClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!")),
            (request, requiredClass) -> {
                EquipmentRequirement requirement = new EquipmentRequirement();
                requirement.setEquipment(equipment);
                requirement.setRequiredClass(requiredClass);
                requirement.setMinStrength(normalize(request.minStrength()));
                requirement.setMinDexterity(normalize(request.minDexterity()));
                requirement.setMinConstitution(normalize(request.minConstitution()));
                requirement.setMinIntelligence(normalize(request.minIntelligence()));
                requirement.setMinWisdom(normalize(request.minWisdom()));
                requirement.setMinCharisma(normalize(request.minCharisma()));
                return requirement;
            }
        );
    }

    private void replaceRequirements(Equipment equipment, List<UpdateEquipmentRequest.RequirementRequest> requests) {
        if (equipment.getRequirements() == null) {
            equipment.setRequirements(new java.util.ArrayList<>());
        } else {
            equipment.getRequirements().clear();
        }

        equipment.getRequirements().addAll(RequirementMapperHelper.mapRequirements(
            requests,
            "equipamento",
            UpdateEquipmentRequest.RequirementRequest::requiredClassId,
            classId -> characterClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!")),
            (request, requiredClass) -> {
                EquipmentRequirement requirement = new EquipmentRequirement();
                requirement.setEquipment(equipment);
                requirement.setRequiredClass(requiredClass);
                requirement.setMinStrength(normalize(request.minStrength()));
                requirement.setMinDexterity(normalize(request.minDexterity()));
                requirement.setMinConstitution(normalize(request.minConstitution()));
                requirement.setMinIntelligence(normalize(request.minIntelligence()));
                requirement.setMinWisdom(normalize(request.minWisdom()));
                requirement.setMinCharisma(normalize(request.minCharisma()));
                return requirement;
            }
        ));
    }

    private Integer normalize(Integer value) {
        return value != null ? value : 0;
    }
}



