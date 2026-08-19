package com.eduardo.rpg.Character.Service;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.Character.DTO.CharacterMapper;
import com.eduardo.rpg.Character.DTO.CharacterResponseDTO;
import com.eduardo.rpg.Character.DTO.CharacterAbilityValidationDTO;
import com.eduardo.rpg.Character.DTO.CharacterEquipmentValidationDTO;
import com.eduardo.rpg.Character.DTO.CreateCharacterRequest;
import com.eduardo.rpg.Character.DTO.UpdateCharacterRequest;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import com.eduardo.rpg.AbilitySpell.Repository.AbilitySpellRepository;
import com.eduardo.rpg.CharacterStatus.CharacterStatus;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.Race.Repository.RaceRepository;
import com.eduardo.rpg.Equipment.Equipment;
import com.eduardo.rpg.Equipment.Repository.EquipmentRepository;
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
    private final EquipmentRepository equipmentRepository;
    private final AbilitySpellRepository abilitySpellRepository;
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
    public Page<CharacterResponseDTO> findCharactersByUserId(Authentication authentication, Long userId, Pageable pageable) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireSameUserOrAdmin(authUser, userId);

        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        return characterRepository.findByUserId(userId, pageable)
            .map(characterMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CharacterResponseDTO> findCharactersByCampaignId(Authentication authentication, Long campaignId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        // Allow viewing if admin, campaign master, or a participating player (players can only see their own characters)
        if (accessControlService.isAdmin(authUser) || accessControlService.isCampaignMaster(authUser, campaign)) {
            return characterRepository.findByCampaignId(campaignId)
                .stream()
                .map(characterMapper::toResponse)
                .toList();
        }

        if (accessControlService.isCampaignPlayer(authUser, campaign)) {
            return characterRepository.findByCampaignIdAndUserId(campaignId, authUser.getId())
                .stream()
                .map(characterMapper::toResponse)
                .toList();
        }

        throw new AccessDeniedException("Sem permissão para acessar esta campanha");
    }

    @Transactional(readOnly = true)
    public Page<CharacterResponseDTO> findAllCharacters(Authentication authentication, Pageable pageable) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);

        if (accessControlService.isAdmin(authUser)) {
            return characterRepository.findAll(pageable).map(characterMapper::toResponse);
        }

        // For non-admins return characters the user can access: characters they own or characters in campaigns they master
        Page<Character> page = characterRepository.findByCampaign_Master_IdOrUser_Id(authUser.getId(), authUser.getId(), pageable);
        if (page == null) {
            return new org.springframework.data.domain.PageImpl<>(new java.util.ArrayList<>(), pageable, 0);
        }

        return page.map(characterMapper::toResponse);
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

    @Transactional
    @SuppressWarnings("unused")
    public void addEquipmentToCharacter(Authentication authentication, Long characterId, Long equipmentId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Character character = findAuthorizedCharacter(authUser, characterId);
        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Equipamento não encontrado!"));

        if (character.getEquipments() == null) {
            character.setEquipments(new ArrayList<>());
        }
        if (character.getEquipments().contains(equipment)) {
            throw new IllegalArgumentException("Equipamento já associado ao personagem");
        }

        character.getEquipments().add(equipment);
        if (equipment.getCharacters() != null && !equipment.getCharacters().contains(character)) {
            equipment.getCharacters().add(character);
        }

        characterRepository.save(character);
    }

    @Transactional
    @SuppressWarnings("unused")
    public void removeEquipmentFromCharacter(Authentication authentication, Long characterId, Long equipmentId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Character character = findAuthorizedCharacter(authUser, characterId);
        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Equipamento não encontrado!"));

        if (character.getEquipments() == null || !character.getEquipments().remove(equipment)) {
            throw new ResourceNotFoundException("Equipamento não associado ao personagem!");
        }

        if (equipment.getCharacters() != null) {
            equipment.getCharacters().remove(character);
        }

        characterRepository.save(character);
    }

    @Transactional
    @SuppressWarnings("unused")
    public void addAbilityToCharacter(Authentication authentication, Long characterId, Long abilityId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Character character = findAuthorizedCharacter(authUser, characterId);
        AbilitySpell abilitySpell = abilitySpellRepository.findById(abilityId)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));

        if (character.getAbilities() == null) {
            character.setAbilities(new ArrayList<>());
        }
        if (character.getAbilities().contains(abilitySpell)) {
            throw new IllegalArgumentException("Habilidade/Magia já associada ao personagem");
        }

        character.getAbilities().add(abilitySpell);

        characterRepository.save(character);
    }

    @Transactional
    @SuppressWarnings("unused")
    public void removeAbilityFromCharacter(Authentication authentication, Long characterId, Long abilityId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Character character = findAuthorizedCharacter(authUser, characterId);
        AbilitySpell abilitySpell = abilitySpellRepository.findById(abilityId)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));

        if (character.getAbilities() == null || !character.getAbilities().remove(abilitySpell)) {
            throw new ResourceNotFoundException("Habilidade/Magia não associada ao personagem!");
        }

        characterRepository.save(character);
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

    private Character findAuthorizedCharacter(User authUser, Long characterId) {
        Character character = characterRepository.findById(characterId)
            .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado!"));
        accessControlService.requireCharacterOwnerCampaignOrAdmin(authUser, character);
        return character;
    }

    @Transactional(readOnly = true)
    public CharacterAbilityValidationDTO validateAbilityUsage(Authentication authentication, Long characterId, Long abilityId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Character character = findAuthorizedCharacter(authUser, characterId);

        AbilitySpell ability = abilitySpellRepository.findById(abilityId)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));

        List<CharacterAbilityValidationDTO.RequirementCheckDTO> checks = new ArrayList<>();
        boolean canUse = true;

        for (var requirement : ability.getRequirements()) {
            boolean meetsLevel = requirement.getMinLevel() == null || character.getLevel() >= requirement.getMinLevel();
            boolean meetsStrength = requirement.getMinStrength() == null || getCharacterStat(character, "strength") >= requirement.getMinStrength();
            boolean meetsDexterity = requirement.getMinDexterity() == null || getCharacterStat(character, "dexterity") >= requirement.getMinDexterity();
            boolean meetsConstitution = requirement.getMinConstitution() == null || getCharacterStat(character, "constitution") >= requirement.getMinConstitution();
            boolean meetsIntelligence = requirement.getMinIntelligence() == null || getCharacterStat(character, "intelligence") >= requirement.getMinIntelligence();
            boolean meetsWisdom = requirement.getMinWisdom() == null || getCharacterStat(character, "wisdom") >= requirement.getMinWisdom();
            boolean meetsCharisma = requirement.getMinCharisma() == null || getCharacterStat(character, "charisma") >= requirement.getMinCharisma();

            boolean canUseForThisClass = character.getCharacterClass().getId().equals(requirement.getRequiredClass().getId()) &&
                meetsLevel && meetsStrength && meetsDexterity && meetsConstitution && meetsIntelligence && meetsWisdom && meetsCharisma;

            if (!canUseForThisClass) {
                canUse = false;
            }

            checks.add(new CharacterAbilityValidationDTO.RequirementCheckDTO(
                requirement.getRequiredClass().getName(),
                meetsLevel,
                requirement.getMinLevel(),
                character.getLevel(),
                meetsStrength,
                requirement.getMinStrength(),
                getCharacterStat(character, "strength"),
                meetsDexterity,
                requirement.getMinDexterity(),
                getCharacterStat(character, "dexterity"),
                meetsConstitution,
                requirement.getMinConstitution(),
                getCharacterStat(character, "constitution"),
                meetsIntelligence,
                requirement.getMinIntelligence(),
                getCharacterStat(character, "intelligence"),
                meetsWisdom,
                requirement.getMinWisdom(),
                getCharacterStat(character, "wisdom"),
                meetsCharisma,
                requirement.getMinCharisma(),
                getCharacterStat(character, "charisma"),
                canUseForThisClass
            ));
        }

        return new CharacterAbilityValidationDTO(characterId, abilityId, ability.getName(), canUse, checks);
    }

    @Transactional(readOnly = true)
    public CharacterEquipmentValidationDTO validateEquipmentUsage(Authentication authentication, Long characterId, Long equipmentId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Character character = findAuthorizedCharacter(authUser, characterId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Equipamento não encontrado!"));

        List<CharacterEquipmentValidationDTO.RequirementCheckDTO> checks = new ArrayList<>();
        boolean canEquip = true;

        for (var requirement : equipment.getRequirements()) {
            boolean meetsStrength = requirement.getMinStrength() == null || getCharacterStat(character, "strength") >= requirement.getMinStrength();
            boolean meetsDexterity = requirement.getMinDexterity() == null || getCharacterStat(character, "dexterity") >= requirement.getMinDexterity();
            boolean meetsConstitution = requirement.getMinConstitution() == null || getCharacterStat(character, "constitution") >= requirement.getMinConstitution();
            boolean meetsIntelligence = requirement.getMinIntelligence() == null || getCharacterStat(character, "intelligence") >= requirement.getMinIntelligence();
            boolean meetsWisdom = requirement.getMinWisdom() == null || getCharacterStat(character, "wisdom") >= requirement.getMinWisdom();
            boolean meetsCharisma = requirement.getMinCharisma() == null || getCharacterStat(character, "charisma") >= requirement.getMinCharisma();

            boolean canEquipForThisClass = character.getCharacterClass().getId().equals(requirement.getRequiredClass().getId()) &&
                meetsStrength && meetsDexterity && meetsConstitution && meetsIntelligence && meetsWisdom && meetsCharisma;

            if (!canEquipForThisClass) {
                canEquip = false;
            }

            checks.add(new CharacterEquipmentValidationDTO.RequirementCheckDTO(
                requirement.getRequiredClass().getName(),
                meetsStrength,
                requirement.getMinStrength(),
                getCharacterStat(character, "strength"),
                meetsDexterity,
                requirement.getMinDexterity(),
                getCharacterStat(character, "dexterity"),
                meetsConstitution,
                requirement.getMinConstitution(),
                getCharacterStat(character, "constitution"),
                meetsIntelligence,
                requirement.getMinIntelligence(),
                getCharacterStat(character, "intelligence"),
                meetsWisdom,
                requirement.getMinWisdom(),
                getCharacterStat(character, "wisdom"),
                meetsCharisma,
                requirement.getMinCharisma(),
                getCharacterStat(character, "charisma"),
                canEquipForThisClass
            ));
        }

        return new CharacterEquipmentValidationDTO(characterId, equipmentId, equipment.getName(), canEquip, checks);
    }

    private Integer getCharacterStat(Character character, String stat) {
        if (character.getCharacterClass() == null) {
            return 0;
        }

        int baseStat = 10; // D&D standard base stat
        CharacterClass charClass = character.getCharacterClass();
        Race race = character.getRace();

        return switch (stat.toLowerCase()) {
            case "strength" -> baseStat + (charClass.getStrengthBonus() != null ? charClass.getStrengthBonus() : 0) +
                            (race != null && race.getStrengthBonus() != null ? race.getStrengthBonus() : 0);
            case "dexterity" -> baseStat + (charClass.getDexterityBonus() != null ? charClass.getDexterityBonus() : 0) +
                             (race != null && race.getDexterityBonus() != null ? race.getDexterityBonus() : 0);
            case "constitution" -> baseStat + (charClass.getConstitutionBonus() != null ? charClass.getConstitutionBonus() : 0) +
                               (race != null && race.getConstitutionBonus() != null ? race.getConstitutionBonus() : 0);
            case "intelligence" -> baseStat + (charClass.getIntelligenceBonus() != null ? charClass.getIntelligenceBonus() : 0) +
                               (race != null && race.getIntelligenceBonus() != null ? race.getIntelligenceBonus() : 0);
            case "wisdom" -> baseStat + (charClass.getWisdomBonus() != null ? charClass.getWisdomBonus() : 0) +
                          (race != null && race.getWisdomBonus() != null ? race.getWisdomBonus() : 0);
            case "charisma" -> baseStat + (charClass.getCharismaBonus() != null ? charClass.getCharismaBonus() : 0) +
                            (race != null && race.getCharismaBonus() != null ? race.getCharismaBonus() : 0);
            default -> 0;
        };
    }
}

