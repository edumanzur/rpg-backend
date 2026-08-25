package com.eduardo.rpg.StatusTemplate;

import static org.junit.jupiter.api.Assertions.*;

import com.eduardo.rpg.Campaign.DTO.CreateCampaignRequest;
import com.eduardo.rpg.Campaign.DTO.CreateStatusTemplateRequest;
import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.Campaign.Service.CampaignService;
import com.eduardo.rpg.Campaign.Service.StatusTemplateService;
import com.eduardo.rpg.Character.DTO.CreateCharacterRequest;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.Character.Service.CharacterService;
import com.eduardo.rpg.Race.Repository.RaceRepository;
import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.CharacterStatus.CharacterStatus;
import com.eduardo.rpg.CharacterStatus.Repository.CharacterStatusRepository;
import com.eduardo.rpg.CharacterStatus.Service.CharacterStatusService;
import com.eduardo.rpg.Character.DTO.CharacterResponseDTO;
import com.eduardo.rpg.StatusTemplate.Repository.StatusTemplateRepository;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.enums.Gender;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.Race.Race;
import com.eduardo.rpg.CharacterStatus.DTO.UpdateCharacterStatusRequest;
import com.eduardo.rpg.Campaign.DTO.StatusTemplateResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class StatusMechanicsIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private CharacterClassRepository characterClassRepository;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private StatusTemplateService statusTemplateService;

    @Autowired
    private CharacterStatusService characterStatusService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private StatusTemplateRepository statusTemplateRepository;

    @Autowired
    private CharacterStatusRepository characterStatusRepository;

    @Test
    void shouldCreateCharacterStatusesAndBackfillNewTemplates() {
        User master = new User();
        master.setUsername("master-status");
        master.setEmail("master-status@example.com");
        master.setPassword("secret");
        master.setRole(Role.PLAYER);
        master = userRepository.save(master);

        User player = new User();
        player.setUsername("player-status");
        player.setEmail("player-status@example.com");
        player.setPassword("secret");
        player.setRole(Role.PLAYER);
        player = userRepository.save(player);

        Race race = new Race();
        race.setName("Human-status");
        race.setDescription("Versatile");
        race = raceRepository.save(race);

        CharacterClass characterClass = new CharacterClass();
        characterClass.setName("Warrior-status");
        characterClass.setDescription("Front line");
        characterClass = characterClassRepository.save(characterClass);

        Authentication masterAuth = new UsernamePasswordAuthenticationToken(master.getUsername(), "secret", List.of(new SimpleGrantedAuthority("ROLE_MASTER")));
        Authentication playerAuth = new UsernamePasswordAuthenticationToken(player.getUsername(), "secret", List.of(new SimpleGrantedAuthority("ROLE_PLAYER")));

        CreateCampaignRequest createCampaignRequest = new CreateCampaignRequest(
            "Campaign with statuses",
            "Testing custom status templates",
            true,
            List.of(
                new CreateStatusTemplateRequest("HP", "Hit points", 20, 0, 100),
                new CreateStatusTemplateRequest("Mana", "Magic points", 10, 0, 50)
            )
        );

        CampaignResponseDTO createdCampaign = campaignService.createCampaign(masterAuth, master.getId(), createCampaignRequest);
        Long campaignId = createdCampaign.id();
        assertNotNull(campaignRepository.findById(campaignId).orElseThrow());
        assertEquals(2, statusTemplateRepository.findByCampaignId(campaignId).size());

        CreateCharacterRequest createCharacterRequest = new CreateCharacterRequest(
            "Hero",
            race.getId(),
            characterClass.getId(),
            CharacterRole.PLAYER,
            Gender.MALE,
            campaignId,
            1,
            "A hero",
            10, 10, 10, 10, 10, 10
        );

        CharacterResponseDTO characterResponse = characterService.createCharacter(playerAuth, player.getId(), createCharacterRequest);
        assertNotNull(characterResponse);
        assertTrue(characterRepository.findById(characterResponse.id()).isPresent());

        List<CharacterStatus> initialStatuses = characterStatusRepository.findByCharacterId(characterResponse.id());
        assertEquals(2, initialStatuses.size());
        assertTrue(initialStatuses.stream().anyMatch(status -> "HP".equals(status.getTemplate().getName()) && status.getCurrentValue().equals(20)));
        assertTrue(initialStatuses.stream().anyMatch(status -> "Mana".equals(status.getTemplate().getName()) && status.getCurrentValue().equals(10)));

        StatusTemplateResponseDTO staminaTemplate = statusTemplateService.createStatusTemplate(
            masterAuth,
            campaignId,
            new CreateStatusTemplateRequest("Stamina", "Physical endurance", 15, 0, 40)
        );
        assertNotNull(staminaTemplate);

        List<CharacterStatus> backfilledStatuses = characterStatusRepository.findByCharacterId(characterResponse.id());
        assertEquals(3, backfilledStatuses.size());
        CharacterStatus staminaStatus = backfilledStatuses.stream()
            .filter(status -> "Stamina".equals(status.getTemplate().getName()))
            .findFirst()
            .orElseThrow();
        assertEquals(15, staminaStatus.getCurrentValue());

        var updated = characterStatusService.updateCharacterStatus(
            playerAuth,
            characterResponse.id(),
            staminaStatus.getId(),
            new UpdateCharacterStatusRequest(18)
        );
        assertEquals(18, updated.currentValue());

        assertThrows(IllegalArgumentException.class, () -> characterStatusService.updateCharacterStatus(
            playerAuth,
            characterResponse.id(),
            staminaStatus.getId(),
            new UpdateCharacterStatusRequest(41)
        ));
    }
}




