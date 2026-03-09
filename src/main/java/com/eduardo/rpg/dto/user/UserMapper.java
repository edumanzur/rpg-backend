package com.eduardo.rpg.dto.user;

import org.springframework.stereotype.Component;

import com.eduardo.rpg.entity.User;

@Component
public class UserMapper {
    // Converte a Entidade para o DTO de resposta
    public UserResponseDTO toResponse(User user) {
        if (user == null) return null;

        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    // Converte o Request para a Entidade (para o createUser)
    public User toEntity(CreateUserRequest dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        
        return user;
    }
}
