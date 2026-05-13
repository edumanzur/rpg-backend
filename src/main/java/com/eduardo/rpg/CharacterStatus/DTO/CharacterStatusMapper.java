package com.eduardo.rpg.CharacterStatus.DTO;

import com.eduardo.rpg.CharacterStatus.CharacterStatus;
import org.springframework.stereotype.Component;

@Component
public class CharacterStatusMapper {

    public CharacterStatusResponseDTO toResponse(CharacterStatus status) {
        if (status == null) return null;

        return new CharacterStatusResponseDTO(
            status.getId(),
            status.getCharacter() != null ? status.getCharacter().getId() : null,
            status.getTemplate() != null ? status.getTemplate().getId() : null,
            status.getTemplate() != null ? status.getTemplate().getName() : null,
            status.getTemplate() != null ? status.getTemplate().getDescription() : null,
            status.getTemplate() != null ? status.getTemplate().getDefaultValue() : null,
            status.getTemplate() != null ? status.getTemplate().getMinValue() : null,
            status.getTemplate() != null ? status.getTemplate().getMaxValue() : null,
            status.getCurrentValue(),
            status.getCharacter() != null && status.getCharacter().getCampaign() != null ? status.getCharacter().getCampaign().getId() : null,
            status.getCreatedAt(),
            status.getUpdatedAt()
        );
    }
}

