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

import java.util.function.Function;
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
        equipment.setRequirements(mapCreateRequirements(equipment, dto.requirements()));
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return equipmentMapper.toResponse(savedEquipment);
    }

    @Transactional(readOnly = true)
    public EquipmentResponseDTO findEquipmentById(Long id) {
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

    private List<EquipmentRequirement> mapCreateRequirements(Equipment equipment, List<CreateEquipmentRequest.RequirementRequest> requests) {
        return mapRequirementsInternal(
            equipment,
            requests,
            CreateEquipmentRequest.RequirementRequest::requiredClassId,
            CreateEquipmentRequest.RequirementRequest::minStrength,
            CreateEquipmentRequest.RequirementRequest::minDexterity,
            CreateEquipmentRequest.RequirementRequest::minConstitution,
            CreateEquipmentRequest.RequirementRequest::minIntelligence,
            CreateEquipmentRequest.RequirementRequest::minWisdom,
            CreateEquipmentRequest.RequirementRequest::minCharisma
        );
    }

    private List<EquipmentRequirement> mapUpdateRequirements(Equipment equipment, List<UpdateEquipmentRequest.RequirementRequest> requests) {
        return mapRequirementsInternal(
            equipment,
            requests,
            UpdateEquipmentRequest.RequirementRequest::requiredClassId,
            UpdateEquipmentRequest.RequirementRequest::minStrength,
            UpdateEquipmentRequest.RequirementRequest::minDexterity,
            UpdateEquipmentRequest.RequirementRequest::minConstitution,
            UpdateEquipmentRequest.RequirementRequest::minIntelligence,
            UpdateEquipmentRequest.RequirementRequest::minWisdom,
            UpdateEquipmentRequest.RequirementRequest::minCharisma
        );
    }

    private <R> List<EquipmentRequirement> mapRequirementsInternal(
        Equipment equipment,
        List<R> requests,
        Function<R, Long> requiredClassIdExtractor,
        Function<R, Integer> minStrengthExtractor,
        Function<R, Integer> minDexterityExtractor,
        Function<R, Integer> minConstitutionExtractor,
        Function<R, Integer> minIntelligenceExtractor,
        Function<R, Integer> minWisdomExtractor,
        Function<R, Integer> minCharismaExtractor
    ) {
        return RequirementMapperHelper.mapRequirements(
            requests,
            "equipamento",
            requiredClassIdExtractor,
            classId -> characterClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!")),
            (request, requiredClass) -> buildRequirement(
                equipment,
                requiredClass,
                minStrengthExtractor.apply(request),
                minDexterityExtractor.apply(request),
                minConstitutionExtractor.apply(request),
                minIntelligenceExtractor.apply(request),
                minWisdomExtractor.apply(request),
                minCharismaExtractor.apply(request)
            )
        );
    }

    private void replaceRequirements(Equipment equipment, List<UpdateEquipmentRequest.RequirementRequest> requests) {
        if (equipment.getRequirements() == null) {
            equipment.setRequirements(new java.util.ArrayList<>());
        } else {
            equipment.getRequirements().clear();
        }

        equipment.getRequirements().addAll(mapUpdateRequirements(equipment, requests));
    }

    private EquipmentRequirement buildRequirement(
        Equipment equipment,
        com.eduardo.rpg.CharacterClass.CharacterClass requiredClass,
        Integer minStrength,
        Integer minDexterity,
        Integer minConstitution,
        Integer minIntelligence,
        Integer minWisdom,
        Integer minCharisma
    ) {
        EquipmentRequirement requirement = new EquipmentRequirement();
        requirement.setEquipment(equipment);
        requirement.setRequiredClass(requiredClass);
        requirement.setMinStrength(normalize(minStrength));
        requirement.setMinDexterity(normalize(minDexterity));
        requirement.setMinConstitution(normalize(minConstitution));
        requirement.setMinIntelligence(normalize(minIntelligence));
        requirement.setMinWisdom(normalize(minWisdom));
        requirement.setMinCharisma(normalize(minCharisma));
        return requirement;
    }

    private Integer normalize(Integer value) {
        return value != null ? value : 0;
    }
}



