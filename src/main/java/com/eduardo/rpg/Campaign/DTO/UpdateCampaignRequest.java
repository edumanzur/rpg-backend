package com.eduardo.rpg.Campaign.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCampaignRequest(

    @NotBlank(message = "Campaign name is required")
    String name,

    String description,

    @NotNull(message = "Status is required")
    Boolean status
) {}

