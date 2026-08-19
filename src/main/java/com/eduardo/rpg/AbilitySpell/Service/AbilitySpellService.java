package com.eduardo.rpg.AbilitySpell.Service;

import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellMapper;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellRequest;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellResponseDTO;
import com.eduardo.rpg.AbilitySpell.Requirement.AbilityRequirement;
import com.eduardo.rpg.AbilitySpell.Repository.AbilitySpellRepository;
import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.security.AccessControlService;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
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
    private final AccessControlService accessControlService;
    private final CharacterRepository characterRepository;

    @Transactional
    public AbilitySpellResponseDTO createAbilitySpell(Authentication authentication, AbilitySpellRequest dto) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireGameContentWritePermission(authUser, dto.campaignId());
        validateNameAvailability(dto.name(), null);

        AbilitySpell abilitySpell = abilitySpellMapper.toEntity(dto);
        abilitySpell.setRequirements(mapRequirements(abilitySpell, dto.requirements()));
        AbilitySpell savedAbility = abilitySpellRepository.save(abilitySpell);
        return abilitySpellMapper.toResponse(savedAbility);
    }

    @Transactional(readOnly = true)
    public AbilitySpellResponseDTO findAbilitySpellById(Long id) {
        AbilitySpell abilitySpell = abilitySpellRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));
        return abilitySpellMapper.toResponse(abilitySpell);
    }

    @Transactional(readOnly = true)
    public AbilitySpellResponseDTO findAbilitySpellById(Authentication authentication, Long id) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        AbilitySpell abilitySpell = abilitySpellRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));

        if (accessControlService.isAdmin(authUser)) {
            return abilitySpellMapper.toResponse(abilitySpell);
        }

        // Allow if any of the user's characters already has this ability or can use it
        List<Character> userCharacters = characterRepository.findByUserId(authUser.getId());
        boolean allowed = userCharacters.stream().anyMatch(c ->
            (c.getAbilities() != null && c.getAbilities().stream().anyMatch(a -> a.getId().equals(abilitySpell.getId()))) ||
            canCharacterUseAbility(c, abilitySpell)
        );

        if (allowed) return abilitySpellMapper.toResponse(abilitySpell);

        throw new AccessDeniedException("Sem permissão para acessar esta habilidade/magia");
    }

    @Transactional(readOnly = true)
    public Page<AbilitySpellResponseDTO> findAllAbilitySpells(Pageable pageable) {
        return abilitySpellRepository.findAll(pageable)
            .map(abilitySpellMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AbilitySpellResponseDTO> findAllAbilitySpells(Authentication authentication, Long campaignId, Pageable pageable) {
        if (campaignId != null) {
            return abilitySpellRepository.findByCampaignId(campaignId, pageable)
                .map(abilitySpellMapper::toResponse);
        }
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        return abilitySpellRepository.findAll(pageable).map(abilitySpellMapper::toResponse);
    }

    @Transactional
    public AbilitySpellResponseDTO updateAbilitySpell(Authentication authentication, Long id, AbilitySpellRequest dto) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        AbilitySpell abilitySpell = abilitySpellRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));
        accessControlService.requireGameContentWritePermission(authUser, abilitySpell.getCampaignId());

        validateNameAvailability(dto.name(), id);

        abilitySpell = abilitySpellMapper.toEntity(dto, abilitySpell);
        replaceRequirements(abilitySpell, dto.requirements());
        AbilitySpell updatedAbility = abilitySpellRepository.save(abilitySpell);
        return abilitySpellMapper.toResponse(updatedAbility);
    }

    @Transactional
    public void deleteAbilitySpell(Authentication authentication, Long id) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        AbilitySpell abilitySpell = abilitySpellRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Habilidade/Magia não encontrada!"));
        accessControlService.requireGameContentWritePermission(authUser, abilitySpell.getCampaignId());
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

    private List<AbilityRequirement> mapRequirements(AbilitySpell abilitySpell, List<AbilitySpellRequest.RequirementRequest> requests) {
        return RequirementMapperHelper.mapRequirements(
            requests,
            "habilidade",
            AbilitySpellRequest.RequirementRequest::requiredClassId,
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

    private void replaceRequirements(AbilitySpell abilitySpell, List<AbilitySpellRequest.RequirementRequest> requests) {
        if (abilitySpell.getRequirements() == null) {
            abilitySpell.setRequirements(new java.util.ArrayList<>());
        } else {
            abilitySpell.getRequirements().clear();
        }

        abilitySpell.getRequirements().addAll(mapRequirements(abilitySpell, requests));
    }

    private Integer normalize(Integer value) {
        return value != null ? value : 0;
    }

    // Helper: determine if a character can use an ability (same logic as CharacterService.validateAbilityUsage)
    private boolean canCharacterUseAbility(Character character, AbilitySpell ability) {
        if (character == null || ability == null) return false;

        // If ability has no requirements, assume usable
        if (ability.getRequirements() == null || ability.getRequirements().isEmpty()) return true;

        for (var requirement : ability.getRequirements()) {
            boolean meetsLevel = requirement.getMinLevel() == null || character.getLevel() >= requirement.getMinLevel();
            boolean meetsStrength = requirement.getMinStrength() == null || getCharacterStat(character, "strength") >= requirement.getMinStrength();
            boolean meetsDexterity = requirement.getMinDexterity() == null || getCharacterStat(character, "dexterity") >= requirement.getMinDexterity();
            boolean meetsConstitution = requirement.getMinConstitution() == null || getCharacterStat(character, "constitution") >= requirement.getMinConstitution();
            boolean meetsIntelligence = requirement.getMinIntelligence() == null || getCharacterStat(character, "intelligence") >= requirement.getMinIntelligence();
            boolean meetsWisdom = requirement.getMinWisdom() == null || getCharacterStat(character, "wisdom") >= requirement.getMinWisdom();
            boolean meetsCharisma = requirement.getMinCharisma() == null || getCharacterStat(character, "charisma") >= requirement.getMinCharisma();

            boolean canUseForThisClass = character.getCharacterClass() != null && requirement.getRequiredClass() != null &&
                character.getCharacterClass().getId().equals(requirement.getRequiredClass().getId()) &&
                meetsLevel && meetsStrength && meetsDexterity && meetsConstitution && meetsIntelligence && meetsWisdom && meetsCharisma;

            if (canUseForThisClass) return true;
        }

        return false;
    }

    private Integer getCharacterStat(Character character, String stat) {
        if (character.getCharacterClass() == null) {
            return 0;
        }

        int baseStat = 10; // D&D standard base stat
        var charClass = character.getCharacterClass();
        var race = character.getRace();

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

