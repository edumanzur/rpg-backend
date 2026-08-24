package com.eduardo.rpg.Grid.DTO;

import com.eduardo.rpg.Grid.CombatGrid;
import com.eduardo.rpg.Grid.GridToken;
import org.springframework.stereotype.Component;

@Component
public class CombatGridMapper {

    public GridTokenResponseDTO toResponse(GridToken token) {
        if (token == null) return null;

        return new GridTokenResponseDTO(
            token.getId(),
            token.getX(),
            token.getY(),
            token.getLabel(),
            token.getColor(),
            token.getCharacter() != null ? token.getCharacter().getId() : null
        );
    }

    public CombatGridResponseDTO toResponse(CombatGrid grid) {
        if (grid == null) return null;

        return new CombatGridResponseDTO(
            grid.getId(),
            grid.getSession() != null ? grid.getSession().getId() : null,
            grid.getRows(),
            grid.getCols(),
            grid.getTokens() == null ? java.util.List.of() : grid.getTokens().stream().map(this::toResponse).toList(),
            grid.getCreatedAt(),
            grid.getUpdatedAt()
        );
    }
}
