package com.eduardo.rpg.User.DTO;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.enums.Role;

@DisplayName("UserMapper Unit Tests")
class UserMapperTest {

    private UserMapper userMapper;
    private User user;
    private CreateUserRequest createUserRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        now = LocalDateTime.now();
        user = new User(1L, "testuser", "test@example.com", "encoded_password", Role.PLAYER, now, now);
        createUserRequest = new CreateUserRequest("testuser", "test@example.com", "password123");
    }

    @Test
    @DisplayName("Should convert User to UserResponseDTO successfully")
    void testToResponseSuccess() {
        UserResponseDTO response = userMapper.toResponse(user);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals(Role.PLAYER, response.role());
        assertEquals(now, response.createdAt());
    }

    @Test
    @DisplayName("Should return null when converting null User")
    void testToResponseNull() {
        UserResponseDTO response = userMapper.toResponse(null);

        assertNull(response);
    }

    @Test
    @DisplayName("Should convert CreateUserRequest to User entity successfully")
    void testToEntitySuccess() {
        User entity = userMapper.toEntity(createUserRequest);

        assertNotNull(entity);
        assertEquals("testuser", entity.getUsername());
        assertEquals("test@example.com", entity.getEmail());
        assertNull(entity.getPassword());
    }

    @Test
    @DisplayName("Should return null when converting null CreateUserRequest")
    void testToEntityNull() {
        User entity = userMapper.toEntity(null);

        assertNull(entity);
    }

    @Test
    @DisplayName("Should map all fields correctly from User to UserResponseDTO")
    void testToResponseAllFields() {
        User userWithAllFields = new User(
            5L,
            "admin",
            "admin@example.com",
            "hashed_password",
            Role.ADMIN,
            LocalDateTime.of(2024, 1, 1, 10, 30),
            LocalDateTime.of(2024, 6, 1, 15, 45)
        );

        UserResponseDTO response = userMapper.toResponse(userWithAllFields);

        assertEquals(5L, response.id());
        assertEquals("admin", response.username());
        assertEquals("admin@example.com", response.email());
        assertEquals(Role.ADMIN, response.role());
        assertNotNull(response.createdAt());
    }

    @Test
    @DisplayName("Should correctly map UserResponseDTO fields")
    void testToResponseFieldMapping() {
        UserResponseDTO response = userMapper.toResponse(user);

        assertNotNull(response);
        // Verify all expected fields are present
        assertEquals(1L, response.id());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals(Role.PLAYER, response.role());
    }
}


