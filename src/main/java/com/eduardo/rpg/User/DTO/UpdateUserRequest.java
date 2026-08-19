package com.eduardo.rpg.User.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(min = 3, max = 50)
    String username,

    @Email
    String email,

    @Size(min = 6, message = "Password must have at least 6 characters")
    String password
) {}

