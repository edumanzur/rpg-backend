package com.eduardo.rpg.Session.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.Session.DTO.CreateSessionRequest;
import com.eduardo.rpg.Session.DTO.SessionMapper;
import com.eduardo.rpg.Session.DTO.SessionResponseDTO;
import com.eduardo.rpg.Session.DTO.UpdateSessionRequest;
import com.eduardo.rpg.Session.Repository.SessionRepository;
import com.eduardo.rpg.Session.Session;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;

@DisplayName("SessionService Unit Tests")
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionService sessionService;

    private User master;
    private Campaign campaign;
    private Character playerCharacter;
    private Character monsterCharacter;
    private Session session;
    private SessionResponseDTO sessionResponseDTO;
    private CreateSessionRequest createSessionRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        master = new User(1L, "masteruser", "master@example.com", "password", Role.MASTER, null, null);
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Epic Quest");
        campaign.setDescription("A grand adventure");
        campaign.setMaster(master);
        campaign.setStatus(true);

        playerCharacter = new Character();
        playerCharacter.setId(1L);
        playerCharacter.setName("Aragorn");
        playerCharacter.setRole(CharacterRole.PLAYER);
        playerCharacter.setCampaign(campaign);

        monsterCharacter = new Character();
        monsterCharacter.setId(2L);
        monsterCharacter.setName("Orc");
        monsterCharacter.setRole(CharacterRole.MONSTER);
        monsterCharacter.setCampaign(campaign);

        session = new Session();
        session.setId(1L);
        session.setTitle("Sessão 1");
        session.setStory("A emboscada na floresta");
        session.setNotes("Preparar combate inicial");
        session.setCampaign(campaign);
        session.setCharacters(List.of(playerCharacter, monsterCharacter));

        sessionResponseDTO = new SessionResponseDTO(1L, "Sessão 1", "A emboscada na floresta", "Preparar combate inicial", 1L, List.of(1L, 2L), null, null);
        createSessionRequest = new CreateSessionRequest("Sessão 1", "A emboscada na floresta", "Preparar combate inicial", 1L, List.of(1L, 2L));
        authentication = new UsernamePasswordAuthenticationToken("masteruser", "password", List.of(new SimpleGrantedAuthority("ROLE_MASTER")));
    }

    @Test
    @DisplayName("Should create session successfully")
    void testCreateSessionSuccess() {
        Session newSession = new Session();

        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.of(master));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(sessionRepository.existsByTitleAndCampaignId("Sessão 1", 1L)).thenReturn(false);
        when(sessionMapper.toEntity(createSessionRequest)).thenReturn(newSession);
        when(characterRepository.findAllById(any())).thenReturn(List.of(playerCharacter, monsterCharacter));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponseDTO);

        SessionResponseDTO result = sessionService.createSession(authentication, createSessionRequest);

        assertNotNull(result);
        assertEquals("Sessão 1", result.title());
        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when master user not found")
    void testCreateSessionMasterNotFound() {
        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sessionService.createSession(authentication, createSessionRequest));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when authenticated user is not master")
    void testCreateSessionNotMaster() {
        Authentication playerAuth = new UsernamePasswordAuthenticationToken("playeruser", "password", List.of(new SimpleGrantedAuthority("ROLE_PLAYER")));
        User player = new User(2L, "playeruser", "player@example.com", "password", Role.PLAYER, null, null);

        when(userRepository.findByUsername("playeruser")).thenReturn(Optional.of(player));

        assertThrows(IllegalArgumentException.class, () -> sessionService.createSession(playerAuth, createSessionRequest));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when master does not own campaign")
    void testCreateSessionWrongMasterForCampaign() {
        User otherMaster = new User(3L, "othermaster", "other@example.com", "password", Role.MASTER, null, null);
        Campaign otherCampaign = new Campaign();
        otherCampaign.setId(2L);
        otherCampaign.setMaster(otherMaster);
        CreateSessionRequest request = new CreateSessionRequest("Sessão 1", "Story", null, 2L, List.of(1L));

        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.of(master));
        when(campaignRepository.findById(2L)).thenReturn(Optional.of(otherCampaign));

        assertThrows(IllegalArgumentException.class, () -> sessionService.createSession(authentication, request));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when session title already exists")
    void testCreateSessionDuplicateTitle() {
        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.of(master));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(sessionRepository.existsByTitleAndCampaignId("Sessão 1", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> sessionService.createSession(authentication, createSessionRequest));
    }

    @Test
    @DisplayName("Should find session by id successfully")
    void testFindSessionByIdSuccess() {
        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.of(master));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponseDTO);

        SessionResponseDTO result = sessionService.findSessionById(authentication, 1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when session not found")
    void testFindSessionByIdNotFound() {
        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.of(master));
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sessionService.findSessionById(authentication, 1L));
    }

    @Test
    @DisplayName("Should find sessions by campaign id")
    void testFindSessionsByCampaignIdSuccess() {
        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.of(master));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(sessionRepository.findByCampaignId(1L)).thenReturn(List.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponseDTO);

        List<SessionResponseDTO> result = sessionService.findSessionsByCampaignId(authentication, 1L);

        assertEquals(1, result.size());
        assertEquals("Sessão 1", result.get(0).title());
    }

    @Test
    @DisplayName("Should find all sessions")
    void testFindAllSessionsSuccess() {
        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.of(master));
        when(sessionRepository.findAll()).thenReturn(List.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponseDTO);

        List<SessionResponseDTO> result = sessionService.findAllSessions(authentication);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should update session successfully")
    void testUpdateSessionSuccess() {
        UpdateSessionRequest updateRequest = new UpdateSessionRequest("Sessão 1 atualizada", "Nova história", "Novas notas", 1L, List.of(1L, 2L));
        Session updatedSession = new Session();
        updatedSession.setId(1L);
        updatedSession.setTitle("Sessão 1 atualizada");
        updatedSession.setStory("Nova história");
        updatedSession.setNotes("Novas notas");
        updatedSession.setCampaign(campaign);
        updatedSession.setCharacters(List.of(playerCharacter, monsterCharacter));

        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.of(master));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(sessionRepository.existsByTitleAndCampaignIdAndIdNot("Sessão 1 atualizada", 1L, 1L)).thenReturn(false);
        when(sessionMapper.toEntity(updateRequest, session)).thenReturn(updatedSession);
        when(characterRepository.findAllById(any())).thenReturn(List.of(playerCharacter, monsterCharacter));
        when(sessionRepository.save(any(Session.class))).thenReturn(updatedSession);
        when(sessionMapper.toResponse(updatedSession)).thenReturn(new SessionResponseDTO(1L, "Sessão 1 atualizada", "Nova história", "Novas notas", 1L, List.of(1L, 2L), null, null));

        SessionResponseDTO result = sessionService.updateSession(authentication, 1L, updateRequest);

        assertNotNull(result);
        assertEquals("Sessão 1 atualizada", result.title());
    }

    @Test
    @DisplayName("Should delete session successfully")
    void testDeleteSessionSuccess() {
        when(userRepository.findByUsername("masteruser")).thenReturn(Optional.of(master));
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.deleteSession(authentication, 1L);

        verify(sessionRepository, times(1)).delete(session);
    }
}



