package com.eduardo.rpg.User.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(

        @NotBlank @Size(min = 3, max = 50)
        String login,

        @NotBlank @Size(min = 6, message = "Password must have at least 6 characters")
        String password
) {}
