package com.eduardo.rpg.Campaign.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusTemplateRequest(
    @NotBlank(message = "Status name is required")
    String name,

    String description,

    @NotNull(message = "Default value is required")
    Integer defaultValue,

    Integer minValue,

    Integer maxValue
) {}

