package com.eduardo.rpg.Session.Service;

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
import com.eduardo.rpg.security.AccessControlService;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CampaignRepository campaignRepository;
    private final CharacterRepository characterRepository;
    private final SessionMapper sessionMapper;
    private final AccessControlService accessControlService;

    @Transactional
    public SessionResponseDTO createSession(Authentication authentication, CreateSessionRequest dto) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = getMasterCampaign(authUser, dto.campaignId());

        if (sessionRepository.existsByTitleAndCampaignId(dto.title(), campaign.getId())) {
            throw new IllegalArgumentException("Já existe uma sessão com este título nesta campanha");
        }

        Session session = sessionMapper.toEntity(dto);
        session.setCampaign(campaign);
        session.setCharacters(resolveCharactersForCampaign(dto.characterIds(), campaign.getId()));

        Session savedSession = sessionRepository.save(session);
        return sessionMapper.toResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public SessionResponseDTO findSessionById(Authentication authentication, Long id) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        Session session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada!"));

        ensureMasterOwnsCampaign(authUser, session.getCampaign());
        return sessionMapper.toResponse(session);
    }

    @Transactional(readOnly = true)
    public List<SessionResponseDTO> findSessionsByCampaignId(Authentication authentication, Long campaignId) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = getMasterCampaign(authUser, campaignId);

        return sessionRepository.findByCampaignId(campaign.getId())
            .stream()
            .map(sessionMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<SessionResponseDTO> findAllSessions(Authentication authentication, Pageable pageable) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireMasterOrAdmin(authUser);

        return sessionRepository.findAll(pageable)
            .map(sessionMapper::toResponse);
    }

    @Transactional
    public SessionResponseDTO updateSession(Authentication authentication, Long id, UpdateSessionRequest dto) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        Session session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada!"));

        ensureMasterOwnsCampaign(authUser, session.getCampaign());

        Campaign campaign = getMasterCampaign(authUser, dto.campaignId());
        if (sessionRepository.existsByTitleAndCampaignIdAndIdNot(dto.title(), campaign.getId(), id)) {
            throw new IllegalArgumentException("Já existe uma sessão com este título nesta campanha");
        }

        session = sessionMapper.toEntity(dto, session);
        session.setCampaign(campaign);
        session.setCharacters(resolveCharactersForCampaign(dto.characterIds(), campaign.getId()));

        Session updatedSession = sessionRepository.save(session);
        return sessionMapper.toResponse(updatedSession);
    }

    @Transactional
    public void deleteSession(Authentication authentication, Long id) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        Session session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada!"));

        ensureMasterOwnsCampaign(authUser, session.getCampaign());
        sessionRepository.delete(session);
    }

    private Campaign getMasterCampaign(com.eduardo.rpg.User.Domains.User master, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));

        accessControlService.requireCampaignOwnerOrAdmin(master, campaign);

        return campaign;
    }

    private void ensureMasterOwnsCampaign(com.eduardo.rpg.User.Domains.User master, Campaign campaign) {
        accessControlService.requireCampaignOwnerOrAdmin(master, campaign);
    }

    private List<Character> resolveCharactersForCampaign(List<Long> characterIds, Long campaignId) {
        if (characterIds == null || characterIds.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> requestedIds = characterIds.stream().collect(Collectors.toSet());
        List<Character> characters = characterRepository.findAllById(requestedIds).stream().toList();

        if (characters.size() != requestedIds.size()) {
            throw new ResourceNotFoundException("Um ou mais personagens não foram encontrados!");
        }

        boolean invalidCampaign = characters.stream()
            .anyMatch(character -> character.getCampaign() == null || !campaignId.equals(character.getCampaign().getId()));

        if (invalidCampaign) {
            throw new IllegalArgumentException("Todos os personagens da sessão precisam pertencer à mesma campanha");
        }

        return characters;
    }
}

