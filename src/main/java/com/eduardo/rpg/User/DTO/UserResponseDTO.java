package com.eduardo.rpg.User.DTO;

import java.time.LocalDateTime;

import com.eduardo.rpg.enums.Role;

public record UserResponseDTO (
        Long id,
        String username,
        String email,
        Role role,
        LocalDateTime createdAt
) {}
