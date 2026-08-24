package com.eduardo.rpg.Grid.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateGridTokenRequest(

    @NotNull(message = "X position is required")
    Integer x,

    @NotNull(message = "Y position is required")
    Integer y,

    @NotBlank(message = "Label is required")
    String label,

    String color,

    Long characterId
) {}
