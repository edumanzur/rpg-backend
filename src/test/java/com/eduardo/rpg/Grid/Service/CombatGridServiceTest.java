package com.eduardo.rpg.Grid.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.Grid.CombatGrid;
import com.eduardo.rpg.Grid.DTO.CombatGridMapper;
import com.eduardo.rpg.Grid.DTO.CombatGridResponseDTO;
import com.eduardo.rpg.Grid.DTO.CreateGridTokenRequest;
import com.eduardo.rpg.Grid.DTO.GridTokenResponseDTO;
import com.eduardo.rpg.Grid.DTO.UpdateGridRequest;
import com.eduardo.rpg.Grid.DTO.UpdateGridTokenRequest;
import com.eduardo.rpg.Grid.GridToken;
import com.eduardo.rpg.Grid.Repository.CombatGridRepository;
import com.eduardo.rpg.Grid.Repository.GridTokenRepository;
import com.eduardo.rpg.Session.Repository.SessionRepository;
import com.eduardo.rpg.Session.Session;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;

@DisplayName("CombatGridService Unit Tests")
@ExtendWith(MockitoExtension.class)
class CombatGridServiceTest {

    @Mock
    private CombatGridRepository combatGridRepository;

    @Mock
    private GridTokenRepository gridTokenRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private CharacterRepository characterRepository;

    @Mock
    private CombatGridMapper combatGridMapper;

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private CombatGridService combatGridService;

    private User master;
    private Campaign campaign;
    private Session session;
    private CombatGrid grid;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        master = new User(1L, "masteruser", "master@example.com", "password", Role.PLAYER, null, null);
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Epic Quest");
        campaign.setMaster(master);

        session = new Session();
        session.setId(1L);
        session.setTitle("Session 1");
        session.setCampaign(campaign);

        grid = new CombatGrid();
        grid.setId(1L);
        grid.setSession(session);
        grid.setRows(20);
        grid.setCols(20);

        authentication = new UsernamePasswordAuthenticationToken("masteruser", "password", List.of(new SimpleGrantedAuthority("ROLE_PLAYER")));
    }

    @Test
    @DisplayName("Should auto-create grid with default dimensions when missing")
    void testGetGridAutoCreates() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(combatGridRepository.findBySessionId(1L)).thenReturn(Optional.empty());
        when(combatGridRepository.save(any(CombatGrid.class))).thenReturn(grid);
        when(combatGridMapper.toResponse(grid)).thenReturn(new CombatGridResponseDTO(1L, 1L, 20, 20, List.of(), null, null));

        CombatGridResponseDTO result = combatGridService.getGrid(authentication, 1L);

        assertNotNull(result);
        assertEquals(20, result.rows());
        verify(combatGridRepository, times(1)).save(any(CombatGrid.class));
    }

    @Test
    @DisplayName("Should return existing grid without creating a new one")
    void testGetGridExisting() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(combatGridRepository.findBySessionId(1L)).thenReturn(Optional.of(grid));
        when(combatGridMapper.toResponse(grid)).thenReturn(new CombatGridResponseDTO(1L, 1L, 20, 20, List.of(), null, null));

        combatGridService.getGrid(authentication, 1L);

        verify(combatGridRepository, never()).save(any(CombatGrid.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when session doesn't exist")
    void testGetGridSessionNotFound() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> combatGridService.getGrid(authentication, 1L));
    }

    @Test
    @DisplayName("Should update grid dimensions")
    void testUpdateGrid() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(combatGridRepository.findBySessionId(1L)).thenReturn(Optional.of(grid));
        when(combatGridRepository.save(any(CombatGrid.class))).thenReturn(grid);
        when(combatGridMapper.toResponse(grid)).thenReturn(new CombatGridResponseDTO(1L, 1L, 30, 30, List.of(), null, null));

        CombatGridResponseDTO result = combatGridService.updateGrid(authentication, 1L, new UpdateGridRequest(30, 30));

        assertEquals(30, result.rows());
    }

    @Test
    @DisplayName("Should deny grid update when caller is not campaign owner or admin")
    void testUpdateGridDenied() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        doThrow(new AccessDeniedException("Sem permissão")).when(accessControlService).requireCampaignOwnerOrAdmin(master, campaign);

        assertThrows(AccessDeniedException.class, () -> combatGridService.updateGrid(authentication, 1L, new UpdateGridRequest(30, 30)));
    }

    @Test
    @DisplayName("Should add a token to the grid")
    void testAddToken() {
        GridToken token = new GridToken();
        token.setId(5L);
        token.setGrid(grid);
        token.setX(1);
        token.setY(2);
        token.setLabel("Goblin");

        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(combatGridRepository.findBySessionId(1L)).thenReturn(Optional.of(grid));
        when(gridTokenRepository.save(any(GridToken.class))).thenReturn(token);
        when(combatGridMapper.toResponse(token)).thenReturn(new GridTokenResponseDTO(5L, 1, 2, "Goblin", null, null));

        GridTokenResponseDTO result = combatGridService.addToken(authentication, 1L, new CreateGridTokenRequest(1, 2, "Goblin", null, null));

        assertNotNull(result);
        assertEquals("Goblin", result.label());
    }

    @Test
    @DisplayName("Should delete a token")
    void testDeleteToken() {
        GridToken token = new GridToken();
        token.setId(5L);
        token.setGrid(grid);

        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(combatGridRepository.findBySessionId(1L)).thenReturn(Optional.of(grid));
        when(gridTokenRepository.findByIdAndGridId(5L, 1L)).thenReturn(Optional.of(token));

        combatGridService.deleteToken(authentication, 1L, 5L);

        verify(gridTokenRepository, times(1)).delete(token);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when token doesn't exist")
    void testUpdateTokenNotFound() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(combatGridRepository.findBySessionId(1L)).thenReturn(Optional.of(grid));
        when(gridTokenRepository.findByIdAndGridId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> combatGridService.updateToken(
            authentication, 1L, 99L, new UpdateGridTokenRequest(1, 1, "X", null, null)));
    }
}
