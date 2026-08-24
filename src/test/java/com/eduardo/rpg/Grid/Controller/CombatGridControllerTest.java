package com.eduardo.rpg.Grid.Controller;

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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.eduardo.rpg.Grid.DTO.CombatGridResponseDTO;
import com.eduardo.rpg.Grid.DTO.GridTokenResponseDTO;
import com.eduardo.rpg.Grid.Service.CombatGridService;
import com.eduardo.rpg.exception.ResourceNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CombatGridController Integration Tests")
class CombatGridControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CombatGridService combatGridService;

    private CombatGridResponseDTO gridResponseDTO;
    private GridTokenResponseDTO tokenResponseDTO;

    @BeforeEach
    void setUp() {
        gridResponseDTO = new CombatGridResponseDTO(1L, 1L, 20, 20, List.of(), null, null);
        tokenResponseDTO = new GridTokenResponseDTO(1L, 2, 3, "Goblin", "#ff0000", null);
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("GET /sessions/{sessionId}/grid should return the grid")
    void testGetGridSuccess() throws Exception {
        when(combatGridService.getGrid(any(), eq(1L))).thenReturn(gridResponseDTO);

        mockMvc.perform(get("/sessions/1/grid").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows", is(20)))
            .andExpect(jsonPath("$.cols", is(20)));
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("PUT /sessions/{sessionId}/grid should update dimensions")
    void testUpdateGridSuccess() throws Exception {
        CombatGridResponseDTO updated = new CombatGridResponseDTO(1L, 1L, 30, 30, List.of(), null, null);
        when(combatGridService.updateGrid(any(), eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/sessions/1/grid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rows\":30,\"cols\":30}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows", is(30)));
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("POST /sessions/{sessionId}/grid/tokens should add a token")
    void testAddTokenSuccess() throws Exception {
        when(combatGridService.addToken(any(), eq(1L), any())).thenReturn(tokenResponseDTO);

        mockMvc.perform(post("/sessions/1/grid/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"x\":2,\"y\":3,\"label\":\"Goblin\",\"color\":\"#ff0000\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.label", is("Goblin")));
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("PUT /sessions/{sessionId}/grid/tokens/{tokenId} should move a token")
    void testUpdateTokenSuccess() throws Exception {
        GridTokenResponseDTO moved = new GridTokenResponseDTO(1L, 5, 6, "Goblin", "#ff0000", null);
        when(combatGridService.updateToken(any(), eq(1L), eq(1L), any())).thenReturn(moved);

        mockMvc.perform(put("/sessions/1/grid/tokens/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"x\":5,\"y\":6,\"label\":\"Goblin\",\"color\":\"#ff0000\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.x", is(5)));
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("DELETE /sessions/{sessionId}/grid/tokens/{tokenId} should remove a token")
    void testDeleteTokenSuccess() throws Exception {
        doNothing().when(combatGridService).deleteToken(any(), eq(1L), eq(1L));

        mockMvc.perform(delete("/sessions/1/grid/tokens/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("GET /sessions/{sessionId}/grid should return 404 when session not found")
    void testGetGridNotFound() throws Exception {
        when(combatGridService.getGrid(any(), eq(1L))).thenThrow(new ResourceNotFoundException("Sessão não encontrada!"));

        mockMvc.perform(get("/sessions/1/grid").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
