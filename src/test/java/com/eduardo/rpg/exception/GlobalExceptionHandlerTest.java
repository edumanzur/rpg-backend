package com.eduardo.rpg.exception;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.eduardo.rpg.User.Service.UserService;
import com.eduardo.rpg.exception.UserAlreadyExistsException;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("GlobalExceptionHandler Integration Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Should handle ResourceNotFoundException with 404 status")
    void testHandleResourceNotFoundException() throws Exception {
        when(userService.findUserById(999L))
            .thenThrow(new ResourceNotFoundException("Usuário não encontrado!"));

        mockMvc.perform(get("/users/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Usuário não encontrado!")))
            .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Should handle UserAlreadyExistsException with 409 status")
    void testHandleUserAlreadyExistsException() throws Exception {
        when(userService.createUser(any()))
            .thenThrow(new UserAlreadyExistsException("Username testuser already exists"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "testuser",
                        "email": "test@example.com",
                        "password": "password123"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code", is("CONFLICT")))
            .andExpect(jsonPath("$.message", is("Username testuser already exists")))
            .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Should have proper error structure with timestamp, message, and code")
    void testExceptionErrorStructure() throws Exception {
        when(userService.findUserById(1L))
            .thenThrow(new ResourceNotFoundException("Resource not found"));

        mockMvc.perform(get("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$", hasKey("timestamp")))
            .andExpect(jsonPath("$", hasKey("message")))
            .andExpect(jsonPath("$", hasKey("code")));
    }

    @Test
    @DisplayName("Should maintain error structure in responses")
    void testErrorResponseStructure() throws Exception {
        when(userService.createUser(any()))
            .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "testuser",
                        "email": "test@example.com",
                        "password": "password123"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code", is("CONFLICT")))
            .andExpect(jsonPath("$.message", notNullValue()))
            .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}



