package com.eduardo.rpg.Campaign.DTO;

import com.eduardo.rpg.Campaign.Campaign;
import org.springframework.stereotype.Component;

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

        return campaign;
    }

    public Campaign toEntity(UpdateCampaignRequest dto, Campaign campaign) {
        if (dto == null) return campaign;

        campaign.setName(dto.name());
        campaign.setDescription(dto.description());
        campaign.setStatus(dto.status());

        return campaign;
    }
}

