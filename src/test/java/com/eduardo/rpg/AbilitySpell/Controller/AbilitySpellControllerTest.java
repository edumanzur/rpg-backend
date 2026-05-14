package com.eduardo.rpg.AbilitySpell.Controller;

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

import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellResponseDTO;
import com.eduardo.rpg.AbilitySpell.Service.AbilitySpellService;
import com.eduardo.rpg.enums.CostType;
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
@WithMockUser(username = "admin", roles = "ADMIN")
@DisplayName("AbilitySpellController Integration Tests")
class AbilitySpellControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AbilitySpellService abilitySpellService;

    private AbilitySpellResponseDTO abilitySpellResponseDTO;

    @BeforeEach
    void setUp() {
        abilitySpellResponseDTO = new AbilitySpellResponseDTO(
            1L,
            "Fireball",
            "3d6",
            "Explosive fire damage",
            "Burn",
            "A powerful fire spell",
            "1 action",
            CostType.ACTION,
            3,
            List.of(new AbilitySpellResponseDTO.RequirementDTO(1L, 1L, "Mage", 3, 0, 0, 0, 2, 0, 0)),
            null,
            null
        );
    }

    @Test
    @DisplayName("GET /ability-spells/{id} should return ability successfully")
    void testFindAbilitySpellByIdSuccess() throws Exception {
        when(abilitySpellService.findAbilitySpellById(1L)).thenReturn(abilitySpellResponseDTO);

        mockMvc.perform(get("/ability-spells/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Fireball")))
            .andExpect(jsonPath("$.costType", is("ACTION")))
            .andExpect(jsonPath("$.requirements", hasSize(1)));

        verify(abilitySpellService, times(1)).findAbilitySpellById(1L);
    }

    @Test
    @DisplayName("GET /ability-spells/{id} should return 404 when ability not found")
    void testFindAbilitySpellByIdNotFound() throws Exception {
        when(abilitySpellService.findAbilitySpellById(1L)).thenThrow(new ResourceNotFoundException("Habilidade/Magia não encontrada!"));

        mockMvc.perform(get("/ability-spells/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Habilidade/Magia não encontrada!")));
    }

    @Test
    @DisplayName("GET /ability-spells should return all abilities")
    void testFindAllAbilitySpellsSuccess() throws Exception {
        AbilitySpellResponseDTO second = new AbilitySpellResponseDTO(
            2L,
            "Ice Bolt",
            "2d8",
            "Cold damage",
            "Freeze",
            "A cold spell",
            "1 action",
            CostType.ACTION,
            2,
            List.of(),
            null,
            null
        );
        when(abilitySpellService.findAllAbilitySpells(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(abilitySpellResponseDTO, second)));

        mockMvc.perform(get("/ability-spells?page=0&size=10").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].name", is("Fireball")))
            .andExpect(jsonPath("$.content[1].name", is("Ice Bolt")));

        verify(abilitySpellService, times(1)).findAllAbilitySpells(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("POST /ability-spells should create ability successfully")
    void testCreateAbilitySpellSuccess() throws Exception {
        when(abilitySpellService.createAbilitySpell(any())).thenReturn(abilitySpellResponseDTO);

        mockMvc.perform(post("/ability-spells")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Fireball",
                        "damage": "3d6",
                        "effect": "Explosive fire damage",
                        "mainStatus": "Burn",
                        "description": "A powerful fire spell",
                        "cost": "1 action",
                        "costType": "ACTION",
                        "requiredLevel": 3,
                        "requirements": [
                            {
                                "requiredClassId": 1,
                                "minLevel": 3,
                                "minStrength": 0,
                                "minDexterity": 0,
                                "minConstitution": 0,
                                "minIntelligence": 2,
                                "minWisdom": 0,
                                "minCharisma": 0
                            }
                        ]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Fireball")))
            .andExpect(jsonPath("$.requirements", hasSize(1)));

        verify(abilitySpellService, times(1)).createAbilitySpell(any());
    }

    @Test
    @DisplayName("POST /ability-spells should handle conflict")
    void testCreateAbilitySpellConflict() throws Exception {
        when(abilitySpellService.createAbilitySpell(any())).thenThrow(new IllegalArgumentException("Já existe uma habilidade/magia com este nome"));

        mockMvc.perform(post("/ability-spells")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Fireball",
                        "damage": "3d6",
                        "effect": "Explosive fire damage",
                        "mainStatus": "Burn",
                        "description": "A powerful fire spell",
                        "cost": "1 action",
                        "costType": "ACTION",
                        "requiredLevel": 3,
                        "requirements": []
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    @DisplayName("PUT /ability-spells/{id} should update ability successfully")
    void testUpdateAbilitySpellSuccess() throws Exception {
        when(abilitySpellService.updateAbilitySpell(eq(1L), any())).thenReturn(abilitySpellResponseDTO);

        mockMvc.perform(put("/ability-spells/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Fireball",
                        "damage": "4d6",
                        "effect": "Stronger fire damage",
                        "mainStatus": "Burn",
                        "description": "An improved fire spell",
                        "cost": "1 action",
                        "costType": "ACTION",
                        "requiredLevel": 4,
                        "requirements": []
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Fireball")));

        verify(abilitySpellService, times(1)).updateAbilitySpell(eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /ability-spells/{id} should delete ability successfully")
    void testDeleteAbilitySpellSuccess() throws Exception {
        doNothing().when(abilitySpellService).deleteAbilitySpell(1L);

        mockMvc.perform(delete("/ability-spells/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(abilitySpellService, times(1)).deleteAbilitySpell(1L);
    }

    @Test
    @DisplayName("DELETE /ability-spells/{id} should return 404 when ability not found")
    void testDeleteAbilitySpellNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Habilidade/Magia não encontrada!"))
            .when(abilitySpellService).deleteAbilitySpell(1L);

        mockMvc.perform(delete("/ability-spells/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Habilidade/Magia não encontrada!")));
    }
}

