package com.eduardo.rpg.CharacterClass.Service;

import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterClass.DTO.CharacterClassMapper;
import com.eduardo.rpg.CharacterClass.DTO.CharacterClassResponseDTO;
import com.eduardo.rpg.CharacterClass.DTO.CreateCharacterClassRequest;
import com.eduardo.rpg.CharacterClass.DTO.UpdateCharacterClassRequest;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import com.eduardo.rpg.AbilitySpell.Repository.AbilitySpellRepository;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CharacterClassService {

    private final CharacterClassRepository characterClassRepository;
    private final AbilitySpellRepository abilitySpellRepository;
    private final CharacterClassMapper characterClassMapper;
    private final AccessControlService accessControlService;

    @Transactional
    public CharacterClassResponseDTO createCharacterClass(Authentication authentication, CreateCharacterClassRequest dto) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireGameContentWritePermission(authUser, dto.campaignId());

        if (characterClassRepository.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Já existe uma classe com este nome");
        }

        CharacterClass characterClass = characterClassMapper.toEntity(dto);
        CharacterClass savedCharacterClass = characterClassRepository.save(characterClass);
        return characterClassMapper.toResponse(savedCharacterClass);
    }

    @Transactional(readOnly = true)
    public CharacterClassResponseDTO findCharacterClassById(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("Classe não encontrada!");
        }

        CharacterClass characterClass = characterClassRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));
        return characterClassMapper.toResponse(characterClass);
    }

    @Transactional(readOnly = true)
    public Page<CharacterClassResponseDTO> findAllCharacterClasses(Long campaignId, Pageable pageable) {
        if (campaignId != null) {
            return characterClassRepository.findByCampaignId(campaignId, pageable)
                .map(characterClassMapper::toResponse);
        }
        return characterClassRepository.findAll(pageable)
            .map(characterClassMapper::toResponse);
    }

    @Transactional
    public CharacterClassResponseDTO updateCharacterClass(Authentication authentication, Long id, UpdateCharacterClassRequest dto) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        CharacterClass characterClass = characterClassRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));
        accessControlService.requireGameContentWritePermission(authUser, characterClass.getCampaignId());

        characterClassRepository.findByNameIgnoreCase(dto.name())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Já existe uma classe com este nome");
            });

        characterClass = characterClassMapper.toEntity(dto, characterClass);
        CharacterClass updatedCharacterClass = characterClassRepository.save(characterClass);
        return characterClassMapper.toResponse(updatedCharacterClass);
    }

    @Transactional
    public void deleteCharacterClass(Authentication authentication, Long id) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        CharacterClass characterClass = characterClassRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));
        accessControlService.requireGameContentWritePermission(authUser, characterClass.getCampaignId());
        characterClassRepository.delete(characterClass);
    }

    @Transactional
    @SuppressWarnings("unused")
    public void addAbilityToClass(Authentication authentication, Long classId, Long abilityId) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        CharacterClass characterClass = characterClassRepository.findById(classId)
            .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));
        accessControlService.requireGameContentWritePermission(authUser, characterClass.getCampaignId());
        AbilitySpell abilitySpell = abilitySpellRepository.findById(abilityId)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));

        if (characterClass.getAbilities() == null) {
            characterClass.setAbilities(new java.util.ArrayList<>());
        }
        if (characterClass.getAbilities().contains(abilitySpell)) {
            throw new IllegalArgumentException("Habilidade/Magia já associada à classe");
        }

        characterClass.getAbilities().add(abilitySpell);
        if (abilitySpell.getClasses() != null && !abilitySpell.getClasses().contains(characterClass)) {
            abilitySpell.getClasses().add(characterClass);
        }

        characterClassRepository.save(characterClass);
    }
}

