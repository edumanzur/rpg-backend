package com.eduardo.rpg.Campaign.DTO;

import com.eduardo.rpg.StatusTemplate.StatusTemplate;
import org.springframework.stereotype.Component;

@Component
public class StatusTemplateMapper {

    public StatusTemplateResponseDTO toResponse(StatusTemplate template) {
        if (template == null) return null;

        return new StatusTemplateResponseDTO(
            template.getId(),
            template.getName(),
            template.getDescription(),
            template.getDefaultValue(),
            template.getMinValue(),
            template.getMaxValue(),
            template.getCampaign() != null ? template.getCampaign().getId() : null,
            template.getCreatedAt(),
            template.getUpdatedAt()
        );
    }

    public StatusTemplate toEntity(CreateStatusTemplateRequest dto) {
        if (dto == null) return null;

        StatusTemplate template = new StatusTemplate();
        template.setName(dto.name());
        template.setDescription(dto.description());
        template.setDefaultValue(dto.defaultValue());
        template.setMinValue(dto.minValue());
        template.setMaxValue(dto.maxValue());
        return template;
    }

    public StatusTemplate toEntity(UpdateStatusTemplateRequest dto, StatusTemplate template) {
        if (dto == null) return template;

        template.setName(dto.name());
        template.setDescription(dto.description());
        template.setDefaultValue(dto.defaultValue());
        template.setMinValue(dto.minValue());
        template.setMaxValue(dto.maxValue());
        return template;
    }
}

