package com.eduardo.rpg.Character.Service;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.Character.DTO.CharacterMapper;
import com.eduardo.rpg.Character.DTO.CharacterResponseDTO;
import com.eduardo.rpg.Character.DTO.CreateCharacterRequest;
import com.eduardo.rpg.Character.DTO.UpdateCharacterRequest;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.CharacterStatus.CharacterStatus;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.Race.Repository.RaceRepository;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.Race.Race;
import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.StatusTemplate.StatusTemplate;
import com.eduardo.rpg.security.AccessControlService;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final CharacterClassRepository characterClassRepository;
    private final RaceRepository raceRepository;
    private final CharacterMapper characterMapper;
    private final AccessControlService accessControlService;

    @Transactional
    public CharacterResponseDTO createCharacter(Authentication authentication, Long userId, CreateCharacterRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        accessControlService.requireCharacterCreationPermission(authUser, userId, dto.role());

        Campaign campaign = campaignRepository.findById(dto.campaignId())
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));

        Race race = raceRepository.findById(dto.raceId())
            .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada!"));

        CharacterClass characterClass = characterClassRepository.findById(dto.classId())
            .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));

        if (characterRepository.existsByNameAndUserId(dto.name(), userId)) {
            throw new IllegalArgumentException("Já existe um personagem com este nome para este usuário");
        }

        if (user.getRole() == com.eduardo.rpg.enums.Role.PLAYER && dto.role() == CharacterRole.PLAYER
            && characterRepository.existsByUserIdAndCampaignIdAndRole(userId, dto.campaignId(), CharacterRole.PLAYER)) {
            throw new IllegalArgumentException("Player já possui personagem nesta campanha");
        }

        Character character = characterMapper.toEntity(dto);
        character.setUser(user);
        character.setCampaign(campaign);
        character.setRace(race);
        character.setCharacterClass(characterClass);
        character.setStatuses(createStatusesForCampaign(campaign, character));

        Character savedCharacter = characterRepository.save(character);
        return characterMapper.toResponse(savedCharacter);
    }

    @Transactional(readOnly = true)
    public CharacterResponseDTO findCharacterById(Authentication authentication, Long id) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado!"));
        accessControlService.requireCharacterOwnerCampaignOrAdmin(authUser, character);
        return characterMapper.toResponse(character);
    }

    @Transactional(readOnly = true)
    public List<CharacterResponseDTO> findCharactersByUserId(Authentication authentication, Long userId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireSameUserOrAdmin(authUser, userId);

        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        return characterRepository.findByUserId(userId)
            .stream()
            .map(characterMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CharacterResponseDTO> findCharactersByCampaignId(Authentication authentication, Long campaignId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        accessControlService.requireCampaignOwnerOrAdmin(authUser, campaign);

        return characterRepository.findByCampaignId(campaignId)
            .stream()
            .map(characterMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<CharacterResponseDTO> findAllCharacters(Authentication authentication, Pageable pageable) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireMasterOrAdmin(authUser);

        return characterRepository.findAll(pageable)
            .map(characterMapper::toResponse);
    }

    @Transactional
    public CharacterResponseDTO updateCharacter(Authentication authentication, Long id, UpdateCharacterRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado!"));
        accessControlService.requireCharacterOwnerCampaignOrAdmin(authUser, character);
        Long previousCampaignId = character.getCampaign() != null ? character.getCampaign().getId() : null;

        Campaign campaign = campaignRepository.findById(dto.campaignId())
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));

        Race race = raceRepository.findById(dto.raceId())
            .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada!"));

        CharacterClass characterClass = characterClassRepository.findById(dto.classId())
            .orElseThrow(() -> new ResourceNotFoundException("Classe não encontrada!"));

        if (character.getUser().getRole() == com.eduardo.rpg.enums.Role.PLAYER && dto.role() == CharacterRole.MONSTER) {
            throw new AccessDeniedException("Player não pode criar monstro");
        }

        character = characterMapper.toEntity(dto, character);
        character.setCampaign(campaign);
        character.setRace(race);
        character.setCharacterClass(characterClass);

        if (!Objects.equals(previousCampaignId, campaign.getId())) {
            replaceStatusesForCampaign(character, campaign);
        }
        Character updatedCharacter = characterRepository.save(character);

        return characterMapper.toResponse(updatedCharacter);
    }

    @Transactional
    public void deleteCharacter(Authentication authentication, Long id) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Character character = characterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado!"));
        accessControlService.requireCharacterOwnerCampaignOrAdmin(authUser, character);
        characterRepository.delete(character);
    }

    private List<CharacterStatus> createStatusesForCampaign(Campaign campaign, Character character) {
        if (campaign == null || campaign.getStatusTemplates() == null || campaign.getStatusTemplates().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        List<CharacterStatus> statuses = new java.util.ArrayList<>();
        for (StatusTemplate template : campaign.getStatusTemplates()) {
            CharacterStatus status = new CharacterStatus();
            status.setCharacter(character);
            status.setTemplate(template);
            status.setCurrentValue(template.getDefaultValue());
            statuses.add(status);
        }

        return statuses;
    }

    private void replaceStatusesForCampaign(Character character, Campaign campaign) {
        if (character.getStatuses() == null) {
            character.setStatuses(new ArrayList<>());
        } else {
            character.getStatuses().clear();
        }

        character.getStatuses().addAll(createStatusesForCampaign(campaign, character));
    }
}

