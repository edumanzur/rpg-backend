package com.eduardo.rpg.CharacterStatus.Controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eduardo.rpg.CharacterStatus.DTO.CharacterStatusResponseDTO;
import com.eduardo.rpg.CharacterStatus.Service.CharacterStatusService;
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

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = "ADMIN")
@DisplayName("CharacterStatusController Integration Tests")
class CharacterStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CharacterStatusService characterStatusService;

    private CharacterStatusResponseDTO statusResponseDTO;

    @BeforeEach
    void setUp() {
        statusResponseDTO = new CharacterStatusResponseDTO(
            1L,
            1L,
            10L,
            "HP",
            "Hit points",
            100,
            0,
            200,
            80,
            5L,
            null,
            null
        );
    }

    @Test
    @DisplayName("GET /characters/{characterId}/statuses should return status list")
    void testFindByCharacterIdSuccess() throws Exception {
        when(characterStatusService.findStatusesByCharacterId(any(), eq(1L))).thenReturn(List.of(statusResponseDTO));

        mockMvc.perform(get("/characters/1/statuses").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].templateName", is("HP")));

        verify(characterStatusService, times(1)).findStatusesByCharacterId(any(), eq(1L));
    }

    @Test
    @DisplayName("GET /characters/{characterId}/statuses/{statusId} should return single status")
    void testFindByIdSuccess() throws Exception {
        when(characterStatusService.findCharacterStatusById(any(), eq(1L), eq(1L))).thenReturn(statusResponseDTO);

        mockMvc.perform(get("/characters/1/statuses/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.characterId", is(1)))
            .andExpect(jsonPath("$.templateName", is("HP")));

        verify(characterStatusService, times(1)).findCharacterStatusById(any(), eq(1L), eq(1L));
    }

    @Test
    @DisplayName("PATCH /characters/{characterId}/statuses/{statusId} should update status")
    void testUpdateSuccess() throws Exception {
        when(characterStatusService.updateCharacterStatus(any(), eq(1L), eq(1L), any())).thenReturn(statusResponseDTO);

        mockMvc.perform(patch("/characters/1/statuses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "currentValue": 90
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.currentValue", is(80)));

        verify(characterStatusService, times(1)).updateCharacterStatus(any(), eq(1L), eq(1L), any());
    }
}

