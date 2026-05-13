package com.eduardo.rpg.Campaign.DTO;

import jakarta.validation.constraints.NotBlank;

public record CreateCampaignRequest(

    @NotBlank(message = "Campaign name is required")
    String name,

    String description,

    Boolean status
) {}

