package com.eduardo.rpg.Campaign.Service;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.DTO.CampaignMapper;
import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.DTO.CreateCampaignRequest;
import com.eduardo.rpg.Campaign.DTO.CreateStatusTemplateRequest;
import com.eduardo.rpg.Campaign.DTO.UpdateCampaignRequest;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.StatusTemplate.StatusTemplate;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.security.AccessControlService;
import com.eduardo.rpg.exception.ResourceNotFoundException;
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
    private final AccessControlService accessControlService;
    private final StatusTemplateValidator statusTemplateValidator;

    @Transactional
    public CampaignResponseDTO createCampaign(Authentication authentication, Long masterId, CreateCampaignRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireMasterOrAdmin(authUser);
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
        campaign.setStatusTemplates(mapStatusTemplates(campaign, dto.statusTemplates()));

        Campaign savedCampaign = campaignRepository.save(campaign);
        return campaignMapper.toResponse(savedCampaign);
    }

    @Transactional(readOnly = true)
    public CampaignResponseDTO findCampaignById(Authentication authentication, Long id) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        accessControlService.requireCampaignOwnerOrAdmin(authUser, campaign);
        return campaignMapper.toResponse(campaign);
    }

    @Transactional(readOnly = true)
    public java.util.List<CampaignResponseDTO> findCampaignsByMasterId(Authentication authentication, Long masterId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireMasterOrAdmin(authUser);
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
    public Page<CampaignResponseDTO> findAllCampaigns(Authentication authentication, Pageable pageable) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireMasterOrAdmin(authUser);

        return campaignRepository.findAll(pageable)
            .map(campaignMapper::toResponse);
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

            StatusTemplate template = new StatusTemplate();
            template.setName(request.name());
            template.setDescription(request.description());
            template.setDefaultValue(request.defaultValue());
            template.setMinValue(request.minValue());
            template.setMaxValue(request.maxValue());
            template.setCampaign(campaign);
            templates.add(template);
        }

        statusTemplateValidator.validateTemplates(templates);
        return templates;
    }
}

