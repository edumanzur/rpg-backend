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
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository;
    private final SessionMapper sessionMapper;

    @Transactional
    public SessionResponseDTO createSession(Authentication authentication, CreateSessionRequest dto) {
        User master = authenticatedMaster(authentication);
        Campaign campaign = getMasterCampaign(master, dto.campaignId());

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
        User master = authenticatedMaster(authentication);
        Session session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada!"));

        ensureMasterOwnsCampaign(master, session.getCampaign());
        return sessionMapper.toResponse(session);
    }

    @Transactional(readOnly = true)
    public List<SessionResponseDTO> findSessionsByCampaignId(Authentication authentication, Long campaignId) {
        User master = authenticatedMaster(authentication);
        Campaign campaign = getMasterCampaign(master, campaignId);

        return sessionRepository.findByCampaignId(campaign.getId())
            .stream()
            .map(sessionMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SessionResponseDTO> findAllSessions(Authentication authentication) {
        authenticatedMaster(authentication);
        return sessionRepository.findAll()
            .stream()
            .map(sessionMapper::toResponse)
            .toList();
    }

    @Transactional
    public SessionResponseDTO updateSession(Authentication authentication, Long id, UpdateSessionRequest dto) {
        User master = authenticatedMaster(authentication);
        Session session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada!"));

        ensureMasterOwnsCampaign(master, session.getCampaign());

        Campaign campaign = getMasterCampaign(master, dto.campaignId());
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
        User master = authenticatedMaster(authentication);
        Session session = sessionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada!"));

        ensureMasterOwnsCampaign(master, session.getCampaign());
        sessionRepository.delete(session);
    }

    private User authenticatedMaster(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Acesso não autenticado");
        }

        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        if (user.getRole() != Role.MASTER) {
            throw new IllegalArgumentException("Somente o mestre pode acessar sessões");
        }

        return user;
    }

    private Campaign getMasterCampaign(User master, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));

        if (campaign.getMaster() == null || !campaign.getMaster().getId().equals(master.getId())) {
            throw new IllegalArgumentException("Somente o mestre dono da campanha pode gerenciar sessões");
        }

        return campaign;
    }

    private void ensureMasterOwnsCampaign(User master, Campaign campaign) {
        if (campaign == null || campaign.getMaster() == null || !campaign.getMaster().getId().equals(master.getId())) {
            throw new IllegalArgumentException("Somente o mestre dono da campanha pode gerenciar sessões");
        }
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

