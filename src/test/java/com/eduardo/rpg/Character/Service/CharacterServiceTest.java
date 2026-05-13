package com.eduardo.rpg.Character.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.Character.DTO.CharacterMapper;
import com.eduardo.rpg.Character.DTO.CharacterResponseDTO;
import com.eduardo.rpg.Character.DTO.CreateCharacterRequest;
import com.eduardo.rpg.Character.DTO.UpdateCharacterRequest;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.Race.Race;
import com.eduardo.rpg.Race.Repository.RaceRepository;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.enums.Gender;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;

@DisplayName("CharacterService Unit Tests")
@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CharacterClassRepository characterClassRepository;

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private CharacterMapper characterMapper;

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private CharacterService characterService;

    private User user;
    private User master;
    private Campaign campaign;
    private CharacterClass characterClass;
    private Race race;
    private Character character;
    private CharacterResponseDTO characterResponseDTO;
    private CreateCharacterRequest createCharacterRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = new User(1L, "testuser", "test@example.com", "password", Role.PLAYER, null, null);
        master = new User(2L, "masteruser", "master@example.com", "password", Role.MASTER, null, null);
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Epic Quest");
        campaign.setDescription("A grand adventure");
        campaign.setMaster(master);
        campaign.setStatus(true);

        characterClass = new CharacterClass();
        characterClass.setId(1L);
        characterClass.setName("Ranger");
        characterClass.setDescription("Skilled wilderness fighter");
        characterClass.setStrengthBonus(1);
        characterClass.setDexterityBonus(2);
        characterClass.setConstitutionBonus(0);
        characterClass.setIntelligenceBonus(0);
        characterClass.setWisdomBonus(1);
        characterClass.setCharismaBonus(0);

        race = new Race();
        race.setId(1L);
        race.setName("Human");
        race.setDescription("Versatile and resilient");
        race.setStrengthBonus(1);
        race.setDexterityBonus(1);
        race.setConstitutionBonus(0);
        race.setIntelligenceBonus(0);
        race.setWisdomBonus(0);
        race.setCharismaBonus(1);

        character = new Character();
        character.setId(1L);
        character.setName("Aragorn");
        character.setRace(race);
        character.setCharacterClass(characterClass);
        character.setRole(CharacterRole.PLAYER);
        character.setGender(Gender.MALE);
        character.setLevel(10);
        character.setExperience(100);
        character.setUser(user);
        character.setCampaign(campaign);
        character.setDescription("A noble ranger");

        characterResponseDTO = new CharacterResponseDTO(1L, "Aragorn", 1L, "Human", "Versatile and resilient", 1, 1, 0, 0, 0, 1, 1L, "Ranger", "Skilled wilderness fighter", 1, 2, 0, 0, 1, 0, CharacterRole.PLAYER, Gender.MALE, 10, 100, "A noble ranger", 1L, 1L, null, null);
        createCharacterRequest = new CreateCharacterRequest("Aragorn", 1L, 1L, CharacterRole.PLAYER, Gender.MALE, 1L, 10, "A noble ranger");
        authentication = new UsernamePasswordAuthenticationToken("testuser", "password", List.of(new SimpleGrantedAuthority("ROLE_PLAYER")));
    }

    @Test
    @DisplayName("Should create character successfully")
    void testCreateCharacterSuccess() {
        Character newCharacter = new Character();
        newCharacter.setUser(user);

        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(characterClass));
        when(raceRepository.findById(1L)).thenReturn(Optional.of(race));
        when(characterRepository.existsByNameAndUserId("Aragorn", 1L)).thenReturn(false);
        when(characterRepository.existsByUserIdAndCampaignIdAndRole(1L, 1L, CharacterRole.PLAYER)).thenReturn(false);
        when(characterMapper.toEntity(createCharacterRequest)).thenReturn(newCharacter);
        when(characterRepository.save(any(Character.class))).thenReturn(character);
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);

        CharacterResponseDTO result = characterService.createCharacter(authentication, 1L, createCharacterRequest);

        assertNotNull(result);
        assertEquals("Aragorn", result.name());
        verify(characterRepository, times(1)).save(any(Character.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testCreateCharacterUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        assertThrows(ResourceNotFoundException.class, () -> characterService.createCharacter(authentication, 1L, createCharacterRequest));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when campaign not found")
    void testCreateCharacterCampaignNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        assertThrows(ResourceNotFoundException.class, () -> characterService.createCharacter(authentication, 1L, createCharacterRequest));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when race not found")
    void testCreateCharacterRaceNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(raceRepository.findById(1L)).thenReturn(Optional.empty());
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        assertThrows(ResourceNotFoundException.class, () -> characterService.createCharacter(authentication, 1L, createCharacterRequest));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when character name exists")
    void testCreateCharacterNameExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(characterClass));
        when(raceRepository.findById(1L)).thenReturn(Optional.of(race));
        when(characterRepository.existsByNameAndUserId("Aragorn", 1L)).thenReturn(true);
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> characterService.createCharacter(authentication, 1L, createCharacterRequest));
    }

    @Test
    @DisplayName("Should find character by id successfully")
    void testFindCharacterByIdSuccess() {
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        CharacterResponseDTO result = characterService.findCharacterById(authentication, 1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Aragorn", result.name());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when character not found")
    void testFindCharacterByIdNotFound() {
        when(characterRepository.findById(1L)).thenReturn(Optional.empty());
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        assertThrows(ResourceNotFoundException.class, () -> characterService.findCharacterById(authentication, 1L));
    }

    @Test
    @DisplayName("Should find characters by user id")
    void testFindCharactersByUserIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(characterRepository.findByUserId(1L)).thenReturn(List.of(character));
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        List<CharacterResponseDTO> result = characterService.findCharactersByUserId(authentication, 1L);

        assertEquals(1, result.size());
        assertEquals("Aragorn", result.get(0).name());
    }

    @Test
    @DisplayName("Should find characters by campaign id")
    void testFindCharactersByCampaignIdSuccess() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(characterRepository.findByCampaignId(1L)).thenReturn(List.of(character));
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        List<CharacterResponseDTO> result = characterService.findCharactersByCampaignId(authentication, 1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should find all characters")
    void testFindAllCharactersSuccess() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(characterRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(character)));
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);

        Page<CharacterResponseDTO> result = characterService.findAllCharacters(authentication, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should update character successfully")
    void testUpdateCharacterSuccess() {
        UpdateCharacterRequest updateRequest = new UpdateCharacterRequest("Aragorn", 1L, 1L, CharacterRole.PLAYER, Gender.MALE, 1L, 11, 150, "Updated ranger");
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(characterClass));
        when(raceRepository.findById(1L)).thenReturn(Optional.of(race));
        when(characterMapper.toEntity(updateRequest, character)).thenReturn(character);
        when(characterRepository.save(any(Character.class))).thenReturn(character);
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        CharacterResponseDTO result = characterService.updateCharacter(authentication, 1L, updateRequest);

        assertNotNull(result);
        verify(characterRepository, times(1)).save(any(Character.class));
    }

    @Test
    @DisplayName("Should delete character successfully")
    void testDeleteCharacterSuccess() {
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        characterService.deleteCharacter(authentication, 1L);

        verify(characterRepository, times(1)).delete(character);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent character")
    void testDeleteCharacterNotFound() {
        when(characterRepository.findById(1L)).thenReturn(Optional.empty());
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(user);

        assertThrows(ResourceNotFoundException.class, () -> characterService.deleteCharacter(authentication, 1L));
    }
}



