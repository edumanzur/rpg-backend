package com.eduardo.rpg.AbilitySpell.Service;

import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellMapper;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellResponseDTO;
import com.eduardo.rpg.AbilitySpell.DTO.CreateAbilitySpellRequest;
import com.eduardo.rpg.AbilitySpell.DTO.UpdateAbilitySpellRequest;
import com.eduardo.rpg.AbilitySpell.Requirement.AbilityRequirement;
import com.eduardo.rpg.AbilitySpell.Repository.AbilitySpellRepository;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
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
public class AbilitySpellService {

    private final AbilitySpellRepository abilitySpellRepository;
    private final CharacterClassRepository characterClassRepository;
    private final AbilitySpellMapper abilitySpellMapper;

    @Transactional
    public AbilitySpellResponseDTO createAbilitySpell(CreateAbilitySpellRequest dto) {
        validateNameAvailability(dto.name(), null);

        AbilitySpell abilitySpell = abilitySpellMapper.toEntity(dto);
        abilitySpell.setRequirements(mapRequirements(abilitySpell, dto.requirements()));
        AbilitySpell savedAbility = abilitySpellRepository.save(abilitySpell);
        return abilitySpellMapper.toResponse(savedAbility);
    }

    @Transactional(readOnly = true)
    public AbilitySpellResponseDTO findAbilitySpellById(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("Habilidade/Magia não encontrada!");
        }

        AbilitySpell abilitySpell = abilitySpellRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));
        return abilitySpellMapper.toResponse(abilitySpell);
    }

    @Transactional(readOnly = true)
    public Page<AbilitySpellResponseDTO> findAllAbilitySpells(Pageable pageable) {
        return abilitySpellRepository.findAll(pageable)
            .map(abilitySpellMapper::toResponse);
    }

    @Transactional
    public AbilitySpellResponseDTO updateAbilitySpell(Long id, UpdateAbilitySpellRequest dto) {
        AbilitySpell abilitySpell = abilitySpellRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));

        validateNameAvailability(dto.name(), id);

        abilitySpell = abilitySpellMapper.toEntity(dto, abilitySpell);
        replaceRequirements(abilitySpell, dto.requirements());
        AbilitySpell updatedAbility = abilitySpellRepository.save(abilitySpell);
        return abilitySpellMapper.toResponse(updatedAbility);
    }

    @Transactional
    public void deleteAbilitySpell(Long id) {
        AbilitySpell abilitySpell = abilitySpellRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));
        abilitySpellRepository.delete(abilitySpell);
    }

    private void validateNameAvailability(String name, Long currentId) {
        if (currentId == null) {
            if (abilitySpellRepository.findByNameIgnoreCase(name).isPresent()) {
                throw new IllegalArgumentException("Já existe uma habilidade/magia com este nome");
            }
            return;
        }

        abilitySpellRepository.findByNameIgnoreCase(name)
            .filter(existing -> !existing.getId().equals(currentId))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Já existe uma habilidade/magia com este nome");
            });
    }

    private List<AbilityRequirement> mapRequirements(AbilitySpell abilitySpell, List<CreateAbilitySpellRequest.RequirementRequest> requests) {
        return RequirementMapperHelper.mapRequirements(
            requests,
            "habilidade",
            CreateAbilitySpellRequest.RequirementRequest::requiredClassId,
            classId -> characterClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!")),
            (request, requiredClass) -> {
                AbilityRequirement requirement = new AbilityRequirement();
                requirement.setAbility(abilitySpell);
                requirement.setRequiredClass(requiredClass);
                requirement.setMinLevel(normalize(request.minLevel()));
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

    private void replaceRequirements(AbilitySpell abilitySpell, List<UpdateAbilitySpellRequest.RequirementRequest> requests) {
        if (abilitySpell.getRequirements() == null) {
            abilitySpell.setRequirements(new java.util.ArrayList<>());
        } else {
            abilitySpell.getRequirements().clear();
        }

        abilitySpell.getRequirements().addAll(RequirementMapperHelper.mapRequirements(
            requests,
            "habilidade",
            UpdateAbilitySpellRequest.RequirementRequest::requiredClassId,
            classId -> characterClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!")),
            (request, requiredClass) -> {
                AbilityRequirement requirement = new AbilityRequirement();
                requirement.setAbility(abilitySpell);
                requirement.setRequiredClass(requiredClass);
                requirement.setMinLevel(normalize(request.minLevel()));
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

