package com.eduardo.rpg.User.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.eduardo.rpg.User.DTO.AuthRequest;

@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authRequest = new AuthRequest("testuser", "password123");
    }

    @Test
    @DisplayName("Should authenticate successfully and return token")
    void testAuthenticateSuccess() {
        String expectedToken = "jwt-token-example";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn(expectedToken);

        String token = authService.authenticate(authRequest);

        assertNotNull(token);
        assertEquals(expectedToken, token);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(authentication);
    }

    @Test
    @DisplayName("Should call authentication manager with correct credentials")
    void testAuthenticateCallsAuthenticationManager() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn("token");

        authService.authenticate(authRequest);

        verify(authenticationManager, times(1)).authenticate(
            argThat(token -> token.getPrincipal().equals("testuser") && token.getCredentials().equals("password123"))
        );
    }
}

