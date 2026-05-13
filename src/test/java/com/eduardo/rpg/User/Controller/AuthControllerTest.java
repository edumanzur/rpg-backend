package com.eduardo.rpg.User.Controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.eduardo.rpg.User.Service.AuthService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("POST /api/auth/authentication should return token successfully")
    void testAuthenticateSuccess() throws Exception {
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(authService.authenticate(any()))
            .thenReturn(expectedToken);

        mockMvc.perform(post("/api/auth/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "login": "testuser",
                        "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedToken));

        verify(authService, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("POST /api/auth/authentication should call authService")
    void testAuthenticateCallsService() throws Exception {
        String expectedToken = "jwt-token-example";

        when(authService.authenticate(any()))
            .thenReturn(expectedToken);

        mockMvc.perform(post("/api/auth/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "login": "testuser",
                        "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(content().string(expectedToken));

        verify(authService, times(1)).authenticate(any());
    }
}




