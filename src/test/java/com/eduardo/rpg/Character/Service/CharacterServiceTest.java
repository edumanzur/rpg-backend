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
import org.mockito.MockitoAnnotations;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.Character.DTO.CharacterMapper;
import com.eduardo.rpg.Character.DTO.CharacterResponseDTO;
import com.eduardo.rpg.Character.DTO.CreateCharacterRequest;
import com.eduardo.rpg.Character.DTO.UpdateCharacterRequest;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;

@DisplayName("CharacterService Unit Tests")
class CharacterServiceTest {

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CharacterMapper characterMapper;

    @InjectMocks
    private CharacterService characterService;

    private User user;
    private User master;
    private Campaign campaign;
    private Character character;
    private CharacterResponseDTO characterResponseDTO;
    private CreateCharacterRequest createCharacterRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User(1L, "testuser", "test@example.com", "password", Role.PLAYER, null, null);
        master = new User(2L, "masteruser", "master@example.com", "password", Role.MASTER, null, null);
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Epic Quest");
        campaign.setDescription("A grand adventure");
        campaign.setMaster(master);
        campaign.setStatus(true);

        character = new Character();
        character.setId(1L);
        character.setName("Aragorn");
        character.setRace("Human");
        character.setClassCharacter("Ranger");
        character.setRole(CharacterRole.PLAYER);
        character.setLevel(10);
        character.setExperience(100);
        character.setUser(user);
        character.setCampaign(campaign);
        character.setDescription("A noble ranger");

        characterResponseDTO = new CharacterResponseDTO(1L, "Aragorn", "Human", "Ranger", CharacterRole.PLAYER, 10, 100, "A noble ranger", 1L, 1L, null, null);
        createCharacterRequest = new CreateCharacterRequest("Aragorn", "Human", "Ranger", CharacterRole.PLAYER, 1L, 10, "A noble ranger");
    }

    @Test
    @DisplayName("Should create character successfully")
    void testCreateCharacterSuccess() {
        Character newCharacter = new Character();
        newCharacter.setUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(characterRepository.existsByNameAndUserId("Aragorn", 1L)).thenReturn(false);
        when(characterRepository.existsByUserIdAndCampaignIdAndRole(1L, 1L, CharacterRole.PLAYER)).thenReturn(false);
        when(characterMapper.toEntity(createCharacterRequest)).thenReturn(newCharacter);
        when(characterRepository.save(any(Character.class))).thenReturn(character);
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);

        CharacterResponseDTO result = characterService.createCharacter(1L, createCharacterRequest);

        assertNotNull(result);
        assertEquals("Aragorn", result.name());
        verify(characterRepository, times(1)).save(any(Character.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void testCreateCharacterUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> characterService.createCharacter(1L, createCharacterRequest));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when campaign not found")
    void testCreateCharacterCampaignNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> characterService.createCharacter(1L, createCharacterRequest));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when character name exists")
    void testCreateCharacterNameExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(characterRepository.existsByNameAndUserId("Aragorn", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> characterService.createCharacter(1L, createCharacterRequest));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when player tries to create monster")
    void testCreateCharacterPlayerCannotCreateMonster() {
        CreateCharacterRequest monsterRequest = new CreateCharacterRequest("Orc", "Orc", "Warrior", CharacterRole.MONSTER, 1L, 5, "Enemy");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        assertThrows(IllegalArgumentException.class, () -> characterService.createCharacter(1L, monsterRequest));
    }

    @Test
    @DisplayName("Should find character by id successfully")
    void testFindCharacterByIdSuccess() {
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);

        CharacterResponseDTO result = characterService.findCharacterById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Aragorn", result.name());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when character not found")
    void testFindCharacterByIdNotFound() {
        when(characterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> characterService.findCharacterById(1L));
    }

    @Test
    @DisplayName("Should find characters by user id")
    void testFindCharactersByUserIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(characterRepository.findByUserId(1L)).thenReturn(List.of(character));
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);

        List<CharacterResponseDTO> result = characterService.findCharactersByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("Aragorn", result.get(0).name());
    }

    @Test
    @DisplayName("Should find characters by campaign id")
    void testFindCharactersByCampaignIdSuccess() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(characterRepository.findByCampaignId(1L)).thenReturn(List.of(character));
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);

        List<CharacterResponseDTO> result = characterService.findCharactersByCampaignId(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should find all characters")
    void testFindAllCharactersSuccess() {
        when(characterRepository.findAll()).thenReturn(List.of(character));
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);

        List<CharacterResponseDTO> result = characterService.findAllCharacters();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should update character successfully")
    void testUpdateCharacterSuccess() {
        UpdateCharacterRequest updateRequest = new UpdateCharacterRequest("Aragorn", "Human", "Ranger", CharacterRole.PLAYER, 1L, 11, 150, "Updated ranger");
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(characterMapper.toEntity(updateRequest, character)).thenReturn(character);
        when(characterRepository.save(any(Character.class))).thenReturn(character);
        when(characterMapper.toResponse(character)).thenReturn(characterResponseDTO);

        CharacterResponseDTO result = characterService.updateCharacter(1L, updateRequest);

        assertNotNull(result);
        verify(characterRepository, times(1)).save(any(Character.class));
    }

    @Test
    @DisplayName("Should delete character successfully")
    void testDeleteCharacterSuccess() {
        when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

        characterService.deleteCharacter(1L);

        verify(characterRepository, times(1)).delete(character);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent character")
    void testDeleteCharacterNotFound() {
        when(characterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> characterService.deleteCharacter(1L));
    }
}



