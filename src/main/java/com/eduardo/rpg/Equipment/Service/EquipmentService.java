package com.eduardo.rpg.Equipment.Service;

import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.Equipment.DTO.CreateEquipmentRequest;
import com.eduardo.rpg.Equipment.DTO.EquipmentMapper;
import com.eduardo.rpg.Equipment.DTO.EquipmentResponseDTO;
import com.eduardo.rpg.Equipment.DTO.UpdateEquipmentRequest;
import com.eduardo.rpg.Equipment.Equipment;
import com.eduardo.rpg.Equipment.Requirement.EquipmentRequirement;
import com.eduardo.rpg.Equipment.Repository.EquipmentRepository;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        equipmentRepository.findByNameIgnoreCase(name)
            .filter(existing -> currentId == null || !existing.getId().equals(currentId))
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
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<EquipmentRequirement> requirements = new ArrayList<>();
        Set<Long> classIds = new HashSet<>();

        for (int i = 0; i < requests.size(); i++) {
            CreateEquipmentRequest.RequirementRequest request = requests.get(i);
            if (request == null) {
                throw new IllegalArgumentException("Requisito de equipamento na posição " + i + " não pode ser nulo");
            }

            if (!classIds.add(request.requiredClassId())) {
                throw new IllegalArgumentException("A mesma classe não pode aparecer mais de uma vez nos requisitos");
            }

            CharacterClass requiredClass = characterClassRepository.findById(request.requiredClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));

            EquipmentRequirement requirement = new EquipmentRequirement();
            requirement.setEquipment(equipment);
            requirement.setRequiredClass(requiredClass);
            requirement.setMinStrength(normalize(request.minStrength()));
            requirement.setMinDexterity(normalize(request.minDexterity()));
            requirement.setMinConstitution(normalize(request.minConstitution()));
            requirement.setMinIntelligence(normalize(request.minIntelligence()));
            requirement.setMinWisdom(normalize(request.minWisdom()));
            requirement.setMinCharisma(normalize(request.minCharisma()));
            requirements.add(requirement);
        }

        return requirements;
    }

    private void replaceRequirements(Equipment equipment, List<UpdateEquipmentRequest.RequirementRequest> requests) {
        if (equipment.getRequirements() == null) {
            equipment.setRequirements(new ArrayList<>());
        } else {
            equipment.getRequirements().clear();
        }

        if (requests == null || requests.isEmpty()) {
            return;
        }

        Set<Long> classIds = new HashSet<>();
        for (int i = 0; i < requests.size(); i++) {
            UpdateEquipmentRequest.RequirementRequest request = requests.get(i);
            if (request == null) {
                throw new IllegalArgumentException("Requisito de equipamento na posição " + i + " não pode ser nulo");
            }

            if (!classIds.add(request.requiredClassId())) {
                throw new IllegalArgumentException("A mesma classe não pode aparecer mais de uma vez nos requisitos");
            }

            CharacterClass requiredClass = characterClassRepository.findById(request.requiredClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));

            EquipmentRequirement requirement = new EquipmentRequirement();
            requirement.setEquipment(equipment);
            requirement.setRequiredClass(requiredClass);
            requirement.setMinStrength(normalize(request.minStrength()));
            requirement.setMinDexterity(normalize(request.minDexterity()));
            requirement.setMinConstitution(normalize(request.minConstitution()));
            requirement.setMinIntelligence(normalize(request.minIntelligence()));
            requirement.setMinWisdom(normalize(request.minWisdom()));
            requirement.setMinCharisma(normalize(request.minCharisma()));
            equipment.getRequirements().add(requirement);
        }
    }

    private Integer normalize(Integer value) {
        return value != null ? value : 0;
    }
}



