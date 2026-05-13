package com.eduardo.rpg.Campaign.DTO;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.StatusTemplate.StatusTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CampaignMapper {

    public CampaignResponseDTO toResponse(Campaign campaign) {
        if (campaign == null) return null;

        return new CampaignResponseDTO(
            campaign.getId(),
            campaign.getName(),
            campaign.getDescription(),
            campaign.getStatus(),
            campaign.getMaster() != null ? campaign.getMaster().getId() : null,
            campaign.getCreatedAt(),
            campaign.getUpdatedAt()
        );
    }

    public Campaign toEntity(CreateCampaignRequest dto) {
        if (dto == null) return null;

        Campaign campaign = new Campaign();
        campaign.setName(dto.name());
        campaign.setDescription(dto.description());
        campaign.setStatus(dto.status() != null ? dto.status() : true);
        campaign.setStatusTemplates(mapStatusTemplates(dto.statusTemplates()));

        return campaign;
    }

    public Campaign toEntity(UpdateCampaignRequest dto, Campaign campaign) {
        if (dto == null) return campaign;

        campaign.setName(dto.name());
        campaign.setDescription(dto.description());
        campaign.setStatus(dto.status());

        return campaign;
    }

    private List<StatusTemplate> mapStatusTemplates(List<CreateStatusTemplateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<StatusTemplate> templates = new ArrayList<>();
        for (CreateStatusTemplateRequest request : requests) {
            StatusTemplate template = new StatusTemplate();
            template.setName(request.name());
            template.setDescription(request.description());
            template.setDefaultValue(request.defaultValue());
            template.setMinValue(request.minValue());
            template.setMaxValue(request.maxValue());
            templates.add(template);
        }

        return templates;
    }
}

