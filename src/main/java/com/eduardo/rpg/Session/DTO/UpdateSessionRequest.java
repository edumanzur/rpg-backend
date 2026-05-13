package com.eduardo.rpg.Session.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateSessionRequest(

    @NotBlank(message = "Session title is required")
    String title,

    @NotBlank(message = "Session story is required")
    String story,

    String notes,

    @NotNull(message = "Campaign is required")
    Long campaignId,

    @NotEmpty(message = "At least one character is required")
    List<Long> characterIds
) {}

