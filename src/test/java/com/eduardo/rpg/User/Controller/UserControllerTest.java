package com.eduardo.rpg.User.Controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.eduardo.rpg.User.DTO.UserResponseDTO;
import com.eduardo.rpg.User.Service.UserService;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userResponseDTO = new UserResponseDTO(1L, "testuser", "test@example.com", Role.PLAYER, null);
    }

    @Test
    @DisplayName("GET /users/{id} should return user successfully")
    void testFindUserByIdSuccess() throws Exception {
        when(userService.findUserById(1L)).thenReturn(userResponseDTO);

        mockMvc.perform(get("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.username", is("testuser")))
            .andExpect(jsonPath("$.email", is("test@example.com")))
            .andExpect(jsonPath("$.role", is("PLAYER")));

        verify(userService, times(1)).findUserById(1L);
    }

    @Test
    @DisplayName("GET /users/{id} should return 404 when user not found")
    void testFindUserByIdNotFound() throws Exception {
        when(userService.findUserById(1L))
            .thenThrow(new ResourceNotFoundException("Usuário não encontrado!"));

        mockMvc.perform(get("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Usuário não encontrado!")));
    }

    @Test
    @DisplayName("GET /users should return all users")
    void testFindAllUsersSuccess() throws Exception {
        UserResponseDTO user2 = new UserResponseDTO(2L, "testuser2", "test2@example.com", Role.PLAYER, null);
        when(userService.findAllUsers(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(userResponseDTO, user2)));

        mockMvc.perform(get("/users?page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].id", is(1)))
            .andExpect(jsonPath("$.content[0].username", is("testuser")))
            .andExpect(jsonPath("$.content[1].id", is(2)))
            .andExpect(jsonPath("$.content[1].username", is("testuser2")));

        verify(userService, times(1)).findAllUsers(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("GET /users should return empty list when no users exist")
    void testFindAllUsersEmpty() throws Exception {
        when(userService.findAllUsers(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/users?page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    @DisplayName("POST /users should create user successfully")
    void testCreateUserSuccess() throws Exception {
        when(userService.createUser(any())).thenReturn(userResponseDTO);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "testuser",
                        "email": "test@example.com",
                        "password": "password123"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.username", is("testuser")))
            .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).createUser(any());
    }

    @Test
    @DisplayName("POST /users should handle conflict when user already exists")
    void testCreateUserWithExistingUser() throws Exception {
        when(userService.createUser(any()))
            .thenThrow(new com.eduardo.rpg.exception.UserAlreadyExistsException("Username already exists"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "existinguser",
                        "email": "test@example.com",
                        "password": "password123"
                    }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /users/{id} should delete user successfully")
    void testDeleteUserSuccess() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("DELETE /users/{id} should return 404 when user not found")
    void testDeleteUserNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Usuário não encontrado!"))
            .when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Usuário não encontrado!")));
    }
}


