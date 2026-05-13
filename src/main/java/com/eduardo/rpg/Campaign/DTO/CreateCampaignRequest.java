package com.eduardo.rpg.Campaign.DTO;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateCampaignRequest(
    @NotBlank(message = "Campaign name is required")
    String name,
    String description,
    Boolean status,
    List<CreateStatusTemplateRequest> statusTemplates
) {

    public CreateCampaignRequest(String name, String description, Boolean status) {
        this(name, description, status, null);
    }
}







