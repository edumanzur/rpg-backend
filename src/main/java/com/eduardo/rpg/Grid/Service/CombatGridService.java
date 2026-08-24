package com.eduardo.rpg.Grid.Service;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Character.Character;
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
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CombatGridService {

    private static final int DEFAULT_ROWS = 20;
    private static final int DEFAULT_COLS = 20;

    private final CombatGridRepository combatGridRepository;
    private final GridTokenRepository gridTokenRepository;
    private final SessionRepository sessionRepository;
    private final CharacterRepository characterRepository;
    private final CombatGridMapper combatGridMapper;
    private final AccessControlService accessControlService;

    @Transactional
    public CombatGridResponseDTO getGrid(Authentication authentication, Long sessionId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Session session = getSessionForRead(authUser, sessionId);

        CombatGrid grid = combatGridRepository.findBySessionId(session.getId())
            .orElseGet(() -> createDefaultGrid(session));

        return combatGridMapper.toResponse(grid);
    }

    @Transactional
    public CombatGridResponseDTO updateGrid(Authentication authentication, Long sessionId, UpdateGridRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Session session = getSessionForWrite(authUser, sessionId);

        CombatGrid grid = combatGridRepository.findBySessionId(session.getId())
            .orElseGet(() -> createDefaultGrid(session));

        grid.setRows(dto.rows());
        grid.setCols(dto.cols());

        CombatGrid saved = combatGridRepository.save(grid);
        return combatGridMapper.toResponse(saved);
    }

    @Transactional
    public GridTokenResponseDTO addToken(Authentication authentication, Long sessionId, CreateGridTokenRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Session session = getSessionForWrite(authUser, sessionId);

        CombatGrid grid = combatGridRepository.findBySessionId(session.getId())
            .orElseGet(() -> createDefaultGrid(session));

        GridToken token = new GridToken();
        token.setGrid(grid);
        token.setX(dto.x());
        token.setY(dto.y());
        token.setLabel(dto.label());
        token.setColor(dto.color());
        token.setCharacter(resolveCharacter(dto.characterId()));

        GridToken saved = gridTokenRepository.save(token);
        return combatGridMapper.toResponse(saved);
    }

    @Transactional
    public GridTokenResponseDTO updateToken(Authentication authentication, Long sessionId, Long tokenId, UpdateGridTokenRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Session session = getSessionForWrite(authUser, sessionId);

        CombatGrid grid = combatGridRepository.findBySessionId(session.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Grid não encontrado!"));

        GridToken token = gridTokenRepository.findByIdAndGridId(tokenId, grid.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Token não encontrado!"));

        token.setX(dto.x());
        token.setY(dto.y());
        token.setLabel(dto.label());
        token.setColor(dto.color());
        token.setCharacter(resolveCharacter(dto.characterId()));

        GridToken saved = gridTokenRepository.save(token);
        return combatGridMapper.toResponse(saved);
    }

    @Transactional
    public void deleteToken(Authentication authentication, Long sessionId, Long tokenId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Session session = getSessionForWrite(authUser, sessionId);

        CombatGrid grid = combatGridRepository.findBySessionId(session.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Grid não encontrado!"));

        GridToken token = gridTokenRepository.findByIdAndGridId(tokenId, grid.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Token não encontrado!"));

        gridTokenRepository.delete(token);
    }

    private CombatGrid createDefaultGrid(Session session) {
        CombatGrid grid = new CombatGrid();
        grid.setSession(session);
        grid.setRows(DEFAULT_ROWS);
        grid.setCols(DEFAULT_COLS);
        return combatGridRepository.save(grid);
    }

    private Character resolveCharacter(Long characterId) {
        if (characterId == null) return null;
        return characterRepository.findById(characterId)
            .orElseThrow(() -> new ResourceNotFoundException("Personagem não encontrado!"));
    }

    private Session getSessionForRead(User authUser, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada!"));
        Campaign campaign = session.getCampaign();
        accessControlService.requireCampaignViewPermission(authUser, campaign);
        return session;
    }

    private Session getSessionForWrite(User authUser, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada!"));
        accessControlService.requireCampaignOwnerOrAdmin(authUser, session.getCampaign());
        return session;
    }
}
