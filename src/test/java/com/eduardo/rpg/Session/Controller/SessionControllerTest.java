package com.eduardo.rpg.Session.Controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eduardo.rpg.Session.DTO.SessionResponseDTO;
import com.eduardo.rpg.Session.Service.SessionService;
import com.eduardo.rpg.exception.ResourceNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SessionController Integration Tests")
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    private SessionResponseDTO sessionResponseDTO;

    @BeforeEach
    void setUp() {
        sessionResponseDTO = new SessionResponseDTO(1L, "Sessão 1", "A emboscada na floresta", "Preparar combate", 1L, List.of(1L, 2L), null, null);
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "MASTER")
    @DisplayName("GET /sessions/{id} should return session successfully for master")
    void testFindSessionByIdSuccess() throws Exception {
        when(sessionService.findSessionById(any(), eq(1L))).thenReturn(sessionResponseDTO);

        mockMvc.perform(get("/sessions/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.title", is("Sessão 1")))
            .andExpect(jsonPath("$.campaignId", is(1)));
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "MASTER")
    @DisplayName("GET /sessions should return all sessions for master")
    void testFindAllSessionsSuccess() throws Exception {
        when(sessionService.findAllSessions(any(), eq(PageRequest.of(0, 10))))
            .thenReturn(new PageImpl<>(List.of(sessionResponseDTO)));

        mockMvc.perform(get("/sessions?page=0&size=10").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "playeruser", roles = "PLAYER")
    @DisplayName("GET /sessions should deny access to player")
    void testFindAllSessionsPlayerDenied() throws Exception {
        mockMvc.perform(get("/sessions?page=0&size=10").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "MASTER")
    @DisplayName("POST /sessions should create session successfully")
    void testCreateSessionSuccess() throws Exception {
        when(sessionService.createSession(any(), any())).thenReturn(sessionResponseDTO);

        mockMvc.perform(post("/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Sessão 1",
                        "story": "A emboscada na floresta",
                        "notes": "Preparar combate",
                        "campaignId": 1,
                        "characterIds": [1,2]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title", is("Sessão 1")));
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "MASTER")
    @DisplayName("DELETE /sessions/{id} should delete session successfully")
    void testDeleteSessionSuccess() throws Exception {
        doNothing().when(sessionService).deleteSession(any(), eq(1L));

        mockMvc.perform(delete("/sessions/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "MASTER")
    @DisplayName("GET /sessions/{id} should return 404 when session not found")
    void testFindSessionByIdNotFound() throws Exception {
        when(sessionService.findSessionById(any(), eq(1L))).thenThrow(new ResourceNotFoundException("Sessão não encontrada!"));

        mockMvc.perform(get("/sessions/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Sessão não encontrada!")));
    }
}

