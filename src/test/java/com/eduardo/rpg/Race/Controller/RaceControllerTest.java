package com.eduardo.rpg.Race.Controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.eduardo.rpg.Race.DTO.RaceResponseDTO;
import com.eduardo.rpg.Race.Service.RaceService;
import com.eduardo.rpg.exception.ResourceNotFoundException;
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

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("RaceController Integration Tests")
class RaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RaceService raceService;

    private RaceResponseDTO raceResponseDTO;

    @BeforeEach
    void setUp() {
        raceResponseDTO = new RaceResponseDTO(1L, "Human", "Versatile and resilient", 1, 1, 0, 0, 0, 1, null, null);
    }

    @Test
    @DisplayName("GET /races/{id} should return race successfully")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testFindRaceByIdSuccess() throws Exception {
        when(raceService.findRaceById(1L)).thenReturn(raceResponseDTO);

        mockMvc.perform(get("/races/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Human")))
            .andExpect(jsonPath("$.strengthBonus", is(1)));

        verify(raceService, times(1)).findRaceById(1L);
    }

    @Test
    @DisplayName("GET /races/{id} should return 404 when race not found")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testFindRaceByIdNotFound() throws Exception {
        when(raceService.findRaceById(1L)).thenThrow(new ResourceNotFoundException("Raça não encontrada!"));

        mockMvc.perform(get("/races/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Raça não encontrada!")));
    }

    @Test
    @DisplayName("GET /races should return all races")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testFindAllRacesSuccess() throws Exception {
        RaceResponseDTO second = new RaceResponseDTO(2L, "Elf", "Graceful and wise", 0, 2, 0, 0, 1, 0, null, null);
        when(raceService.findAllRaces(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(raceResponseDTO, second)));

        mockMvc.perform(get("/races?page=0&size=10").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].name", is("Human")))
            .andExpect(jsonPath("$.content[1].name", is("Elf")));

        verify(raceService, times(1)).findAllRaces(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("POST /races should create race successfully")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateRaceSuccess() throws Exception {
        when(raceService.createRace(any())).thenReturn(raceResponseDTO);

        mockMvc.perform(post("/races")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Human",
                        "description": "Versatile and resilient",
                        "strengthBonus": 1,
                        "dexterityBonus": 1,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 0,
                        "charismaBonus": 1
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Human")));

        verify(raceService, times(1)).createRace(any());
    }

    @Test
    @DisplayName("POST /races should handle conflict")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateRaceConflict() throws Exception {
        when(raceService.createRace(any())).thenThrow(new IllegalArgumentException("Já existe uma raça com este nome"));

        mockMvc.perform(post("/races")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Human",
                        "description": "Versatile and resilient",
                        "strengthBonus": 1,
                        "dexterityBonus": 1,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 0,
                        "charismaBonus": 1
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    @DisplayName("PUT /races/{id} should update race successfully")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateRaceSuccess() throws Exception {
        when(raceService.updateRace(eq(1L), any())).thenReturn(raceResponseDTO);

        mockMvc.perform(put("/races/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Human",
                        "description": "Versatile and resilient",
                        "strengthBonus": 2,
                        "dexterityBonus": 1,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 0,
                        "charismaBonus": 1
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Human")));

        verify(raceService, times(1)).updateRace(eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /races/{id} should delete race successfully")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteRaceSuccess() throws Exception {
        doNothing().when(raceService).deleteRace(1L);

        mockMvc.perform(delete("/races/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(raceService, times(1)).deleteRace(1L);
    }

    @Test
    @DisplayName("DELETE /races/{id} should return 404 when race not found")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteRaceNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Raça não encontrada!"))
            .when(raceService).deleteRace(1L);

        mockMvc.perform(delete("/races/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Raça não encontrada!")));
    }

    @Test
    @DisplayName("POST /races should return 403 when user has PLAYER role")
    @WithMockUser(username = "player", roles = "PLAYER")
    void testCreateRaceWithPlayerRoleShouldForbidden() throws Exception {
        mockMvc.perform(post("/races")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Human",
                        "description": "Versatile and resilient",
                        "strengthBonus": 1,
                        "dexterityBonus": 1,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 0,
                        "charismaBonus": 1
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /races/{id} should return 403 when user has PLAYER role")
    @WithMockUser(username = "player", roles = "PLAYER")
    void testUpdateRaceWithPlayerRoleShouldForbidden() throws Exception {
        mockMvc.perform(put("/races/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Human",
                        "description": "Versatile and resilient",
                        "strengthBonus": 2,
                        "dexterityBonus": 1,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 0,
                        "charismaBonus": 1
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /races/{id} should return 403 when user has PLAYER role")
    @WithMockUser(username = "player", roles = "PLAYER")
    void testDeleteRaceWithPlayerRoleShouldForbidden() throws Exception {
        mockMvc.perform(delete("/races/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /races should be accessible with PLAYER role")
    @WithMockUser(username = "player", roles = "PLAYER")
    void testGetRacesWithPlayerRoShouldSucceed() throws Exception {
        RaceResponseDTO second = new RaceResponseDTO(2L, "Elf", "Graceful and wise", 0, 2, 0, 0, 1, 0, null, null);
        when(raceService.findAllRaces(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(raceResponseDTO, second)));

        mockMvc.perform(get("/races?page=0&size=10").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)));
    }
}

