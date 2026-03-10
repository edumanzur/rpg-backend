package com.eduardo.rpg.dto.user;

import org.springframework.stereotype.Component;

import com.eduardo.rpg.entity.User;

@Component
public class UserMapper {
    // Converte a Entidade para o DTO de resposta
    public UserResponseDTO toResponse(User user) {
        if (user == null) return null;

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    // Converte o Request para a Entidade (para o createUser)
    public User toEntity(CreateUserRequest dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        
        return user;
    }
}
