package com.eduardo.rpg.CharacterStatus.Service;

import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.CharacterStatus.DTO.CharacterStatusMapper;
import com.eduardo.rpg.CharacterStatus.DTO.CharacterStatusResponseDTO;
import com.eduardo.rpg.CharacterStatus.DTO.UpdateCharacterStatusRequest;
import com.eduardo.rpg.CharacterStatus.Repository.CharacterStatusRepository;
import com.eduardo.rpg.CharacterStatus.CharacterStatus;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;
import com.eduardo.rpg.Campaign.Service.StatusTemplateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacterStatusService {

    private final CharacterStatusRepository characterStatusRepository;
    private final CharacterRepository characterRepository;
    private final CharacterStatusMapper characterStatusMapper;
    private final AccessControlService accessControlService;
    private final StatusTemplateValidator statusTemplateValidator;

    @Transactional(readOnly = true)
    public List<CharacterStatusResponseDTO> findStatusesByCharacterId(Authentication authentication, Long characterId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        com.eduardo.rpg.Character.Character character = characterRepository.findById(characterId)
            .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado!"));
        accessControlService.requireCharacterOwnerCampaignOrAdmin(authUser, character);

        return characterStatusRepository.findByCharacterId(characterId)
            .stream()
            .map(characterStatusMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CharacterStatusResponseDTO findCharacterStatusById(Authentication authentication, Long characterId, Long statusId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        CharacterStatus status = findStatusByCharacterAndId(characterId, statusId);
        accessControlService.requireCharacterOwnerCampaignOrAdmin(authUser, status.getCharacter());
        return characterStatusMapper.toResponse(status);
    }

    @Transactional
    public CharacterStatusResponseDTO updateCharacterStatus(Authentication authentication, Long characterId, Long statusId, UpdateCharacterStatusRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        CharacterStatus status = findStatusByCharacterAndId(characterId, statusId);

        accessControlService.requireCharacterOwnerCampaignOrAdmin(authUser, status.getCharacter());
        statusTemplateValidator.validateCurrentValueWithinBounds(status.getTemplate(), dto.currentValue());

        status.setCurrentValue(dto.currentValue());
        CharacterStatus saved = characterStatusRepository.save(status);
        return characterStatusMapper.toResponse(saved);
    }

    private CharacterStatus findStatusByCharacterAndId(Long characterId, Long statusId) {
        CharacterStatus status = characterStatusRepository.findById(statusId)
            .orElseThrow(() -> new ResourceNotFoundException("Status do personagem não encontrado!"));

        if (status.getCharacter() == null || !characterId.equals(status.getCharacter().getId())) {
            throw new ResourceNotFoundException("Status do personagem não encontrado!");
        }

        return status;
    }
}


