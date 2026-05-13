package com.eduardo.rpg.CharacterStatus.DTO;

import jakarta.validation.constraints.NotNull;

public record UpdateCharacterStatusRequest(
    @NotNull(message = "Current value is required")
    Integer currentValue
) {}

