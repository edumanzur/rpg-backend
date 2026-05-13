package com.eduardo.rpg.Campaign.Service;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.DTO.CreateStatusTemplateRequest;
import com.eduardo.rpg.Campaign.DTO.StatusTemplateMapper;
import com.eduardo.rpg.Campaign.DTO.StatusTemplateResponseDTO;
import com.eduardo.rpg.Campaign.DTO.UpdateStatusTemplateRequest;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.Character.Repository.CharacterRepository;
import com.eduardo.rpg.CharacterStatus.CharacterStatus;
import com.eduardo.rpg.CharacterStatus.Repository.CharacterStatusRepository;
import com.eduardo.rpg.StatusTemplate.Repository.StatusTemplateRepository;
import com.eduardo.rpg.StatusTemplate.StatusTemplate;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusTemplateService {

    private final StatusTemplateRepository statusTemplateRepository;
    private final CampaignRepository campaignRepository;
    private final CharacterRepository characterRepository;
    private final CharacterStatusRepository characterStatusRepository;
    private final StatusTemplateMapper statusTemplateMapper;
    private final AccessControlService accessControlService;

    @Transactional(readOnly = true)
    public List<StatusTemplateResponseDTO> findStatusTemplatesByCampaignId(Authentication authentication, Long campaignId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = getCampaign(authUser, campaignId);

        return statusTemplateRepository.findByCampaignId(campaign.getId())
            .stream()
            .map(statusTemplateMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public StatusTemplateResponseDTO findStatusTemplateById(Authentication authentication, Long campaignId, Long templateId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        getCampaign(authUser, campaignId);

        StatusTemplate template = statusTemplateRepository.findByIdAndCampaignId(templateId, campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("StatusTemplate não encontrado!"));
        return statusTemplateMapper.toResponse(template);
    }

    @Transactional
    public StatusTemplateResponseDTO createStatusTemplate(Authentication authentication, Long campaignId, CreateStatusTemplateRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = getCampaign(authUser, campaignId);

        if (statusTemplateRepository.existsByCampaignIdAndNameIgnoreCase(campaignId, dto.name())) {
            throw new IllegalArgumentException("Já existe um status com este nome nesta campanha");
        }

        StatusTemplate template = statusTemplateMapper.toEntity(dto);
        validateTemplateBounds(template);
        template.setCampaign(campaign);

        StatusTemplate savedTemplate = statusTemplateRepository.save(template);
        backfillCharactersWithNewTemplate(savedTemplate);
        return statusTemplateMapper.toResponse(savedTemplate);
    }

    @Transactional
    public StatusTemplateResponseDTO updateStatusTemplate(Authentication authentication, Long campaignId, Long templateId, UpdateStatusTemplateRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        getCampaign(authUser, campaignId);

        StatusTemplate template = statusTemplateRepository.findByIdAndCampaignId(templateId, campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("StatusTemplate não encontrado!"));

        if (!template.getName().equalsIgnoreCase(dto.name())
            && statusTemplateRepository.existsByCampaignIdAndNameIgnoreCase(campaignId, dto.name())) {
            throw new IllegalArgumentException("Já existe um status com este nome nesta campanha");
        }

        template = statusTemplateMapper.toEntity(dto, template);
        validateTemplateBounds(template);
        StatusTemplate savedTemplate = statusTemplateRepository.save(template);
        return statusTemplateMapper.toResponse(savedTemplate);
    }

    @Transactional
    public void deleteStatusTemplate(Authentication authentication, Long campaignId, Long templateId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        getCampaign(authUser, campaignId);

        StatusTemplate template = statusTemplateRepository.findByIdAndCampaignId(templateId, campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("StatusTemplate não encontrado!"));

        characterStatusRepository.deleteByTemplateId(templateId);
        statusTemplateRepository.delete(template);
    }

    private Campaign getCampaign(User authUser, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        accessControlService.requireCampaignOwnerOrAdmin(authUser, campaign);
        return campaign;
    }

    private void backfillCharactersWithNewTemplate(StatusTemplate template) {
        List<com.eduardo.rpg.Character.Character> characters = characterRepository.findByCampaignId(template.getCampaign().getId());
        if (characters.isEmpty()) {
            return;
        }

        List<CharacterStatus> statuses = new ArrayList<>();
        for (com.eduardo.rpg.Character.Character character : characters) {
            if (characterStatusRepository.findByCharacterIdAndTemplateId(character.getId(), template.getId()).isPresent()) {
                continue;
            }

            CharacterStatus status = new CharacterStatus();
            status.setCharacter(character);
            status.setTemplate(template);
            status.setCurrentValue(template.getDefaultValue());
            character.getStatuses().add(status);
            statuses.add(status);
        }

        if (!statuses.isEmpty()) {
            characterStatusRepository.saveAll(statuses);
        }
    }

    private void validateTemplateBounds(StatusTemplate template) {
        if (template.getMinValue() != null && template.getMaxValue() != null && template.getMinValue() > template.getMaxValue()) {
            throw new IllegalArgumentException("O valor mínimo não pode ser maior que o valor máximo");
        }

        if (template.getMinValue() != null && template.getDefaultValue() != null && template.getDefaultValue() < template.getMinValue()) {
            throw new IllegalArgumentException("O defaultValue precisa ser maior ou igual ao valor mínimo");
        }

        if (template.getMaxValue() != null && template.getDefaultValue() != null && template.getDefaultValue() > template.getMaxValue()) {
            throw new IllegalArgumentException("O defaultValue precisa ser menor ou igual ao valor máximo");
        }
    }
}

