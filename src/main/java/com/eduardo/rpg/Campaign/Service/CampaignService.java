package com.eduardo.rpg.Campaign.Service;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.DTO.CampaignMapper;
import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.DTO.CreateCampaignRequest;
import com.eduardo.rpg.Campaign.DTO.CreateStatusTemplateRequest;
import com.eduardo.rpg.Campaign.DTO.StatusTemplateMapper;
import com.eduardo.rpg.Campaign.DTO.UpdateCampaignRequest;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.StatusTemplate.StatusTemplate;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.security.AccessControlService;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.common.CampaignCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CampaignMapper campaignMapper;
    private final StatusTemplateMapper statusTemplateMapper;
    private final AccessControlService accessControlService;
    private final StatusTemplateValidator statusTemplateValidator;

    @Transactional
    public CampaignResponseDTO createCampaignAsCurrentUser(Authentication authentication, CreateCampaignRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);

        if (campaignRepository.existsByNameAndMasterId(dto.name(), authUser.getId())) {
            throw new IllegalArgumentException("Já existe uma campanha com este nome para este mestre");
        }

        Campaign campaign = campaignMapper.toEntity(dto);
        campaign.setMaster(authUser);
        campaign.setInviteCode(CampaignCodeGenerator.generateCode());
        campaign.setStatusTemplates(mapStatusTemplates(campaign, dto.statusTemplates()));

        Campaign savedCampaign = campaignRepository.save(campaign);
        return campaignMapper.toResponse(savedCampaign);
    }

    @Transactional
    public CampaignResponseDTO createCampaign(Authentication authentication, Long masterId, CreateCampaignRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        if (!accessControlService.isAdmin(authUser)) {
            accessControlService.requireSameUserOrAdmin(authUser, masterId);
        }

        User master = userRepository.findById(masterId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        if (campaignRepository.existsByNameAndMasterId(dto.name(), masterId)) {
            throw new IllegalArgumentException("Já existe uma campanha com este nome para este mestre");
        }

        Campaign campaign = campaignMapper.toEntity(dto);
        campaign.setMaster(master);
        campaign.setInviteCode(CampaignCodeGenerator.generateCode());
        campaign.setStatusTemplates(mapStatusTemplates(campaign, dto.statusTemplates()));

        Campaign savedCampaign = campaignRepository.save(campaign);
        return campaignMapper.toResponse(savedCampaign);
    }

    @Transactional(readOnly = true)
    public CampaignResponseDTO findCampaignById(Authentication authentication, Long id) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        accessControlService.requireCampaignViewPermission(authUser, campaign);
        return campaignMapper.toResponse(campaign);
    }

    @Transactional(readOnly = true)
    public CampaignResponseDTO findCampaignByCode(String inviteCode) {
        Campaign campaign = campaignRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Código de campanha inválido!"));
        return campaignMapper.toResponse(campaign);
    }

    @Transactional(readOnly = true)
    public java.util.List<CampaignResponseDTO> findCampaignsByMasterId(Authentication authentication, Long masterId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        if (!accessControlService.isAdmin(authUser)) {
            accessControlService.requireSameUserOrAdmin(authUser, masterId);
        }

        userRepository.findById(masterId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        return campaignRepository.findByMasterId(masterId)
            .stream()
            .map(campaignMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponseDTO> findCampaignsByMasterId(Authentication authentication, Long masterId, Pageable pageable) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        if (!accessControlService.isAdmin(authUser)) {
            accessControlService.requireSameUserOrAdmin(authUser, masterId);
        }

        userRepository.findById(masterId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        return campaignRepository.findByMasterId(masterId, pageable)
            .map(campaignMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponseDTO> findAllCampaigns(Authentication authentication, Pageable pageable) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);

        if (accessControlService.isAdmin(authUser)) {
            return campaignRepository.findAll(pageable).map(campaignMapper::toResponse);
        }

        // A user can master some campaigns and be a joined player in others at
        // the same time, so both sets must be combined — returning only the
        // mastered campaigns whenever that list is non-empty (the previous
        // either/or logic) silently hid every campaign the user had joined via
        // an invite code as soon as they also owned one campaign of their own.
        java.util.List<Campaign> masterCampaigns = campaignRepository.findByMasterId(authUser.getId());
        java.util.List<Campaign> playerCampaigns = campaignRepository.findByPlayersContaining(authUser);

        java.util.LinkedHashMap<Long, Campaign> combined = new java.util.LinkedHashMap<>();
        for (Campaign c : masterCampaigns) combined.put(c.getId(), c);
        for (Campaign c : playerCampaigns) combined.putIfAbsent(c.getId(), c);

        java.util.List<Campaign> all = new java.util.ArrayList<>(combined.values());
        int start = Math.min((int) pageable.getOffset(), all.size());
        int end = Math.min(start + pageable.getPageSize(), all.size());
        java.util.List<CampaignResponseDTO> pageContent = all.subList(start, end)
            .stream()
            .map(campaignMapper::toResponse)
            .collect(java.util.stream.Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, all.size());
    }

    @Transactional
    public CampaignResponseDTO updateCampaign(Authentication authentication, Long id, UpdateCampaignRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        accessControlService.requireCampaignOwnerOrAdmin(authUser, campaign);

        if (campaignRepository.existsByNameAndMasterIdAndIdNot(dto.name(), campaign.getMaster().getId(), id)) {
            throw new IllegalArgumentException("Já existe uma campanha com este nome para este mestre");
        }

        campaign = campaignMapper.toEntity(dto, campaign);
        Campaign updatedCampaign = campaignRepository.save(campaign);

        return campaignMapper.toResponse(updatedCampaign);
    }

    @Transactional
    public void deleteCampaign(Authentication authentication, Long id) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        accessControlService.requireCampaignOwnerOrAdmin(authUser, campaign);
        campaignRepository.delete(campaign);
    }

    @Transactional
    public void joinCampaign(Authentication authentication, String inviteCode) {
        User player = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = campaignRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Código de campanha inválido!"));

        if (campaign.getMaster().getId().equals(player.getId())) {
            throw new IllegalArgumentException("Você é o mestre desta campanha!");
        }

        if (campaign.getPlayers().contains(player)) {
            throw new IllegalArgumentException("Você já está nesta campanha!");
        }

        campaign.getPlayers().add(player);
        campaignRepository.save(campaign);
    }

    private List<StatusTemplate> mapStatusTemplates(Campaign campaign, List<CreateStatusTemplateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<StatusTemplate> templates = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            CreateStatusTemplateRequest request = requests.get(i);
            if (request == null) {
                throw new IllegalArgumentException("Template de status na posição " + i + " não pode ser nulo");
            }

            StatusTemplate template = statusTemplateMapper.toEntity(request);
            template.setCampaign(campaign);
            templates.add(template);
        }

        statusTemplateValidator.validateTemplates(templates);
        return templates;
    }
}

