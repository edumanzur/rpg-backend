package com.eduardo.rpg.Character.Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eduardo.rpg.Character.Service.CharacterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = "ADMIN")
@DisplayName("CharacterController Integration Tests")
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CharacterService characterService;

    @Test
    @DisplayName("POST /characters/{id}/equipments/{equipmentId} should associate equipment successfully")
    void testAddEquipmentToCharacterSuccess() throws Exception {
        doNothing().when(characterService).addEquipmentToCharacter(any(), eq(1L), eq(2L));

        mockMvc.perform(post("/characters/1/equipments/2").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(characterService, times(1)).addEquipmentToCharacter(any(), eq(1L), eq(2L));
    }

    @Test
    @DisplayName("DELETE /characters/{id}/equipments/{equipmentId} should remove equipment successfully")
    void testRemoveEquipmentFromCharacterSuccess() throws Exception {
        doNothing().when(characterService).removeEquipmentFromCharacter(any(), eq(1L), eq(2L));

        mockMvc.perform(delete("/characters/1/equipments/2").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(characterService, times(1)).removeEquipmentFromCharacter(any(), eq(1L), eq(2L));
    }

    @Test
    @DisplayName("POST /characters/{id}/abilities/{abilityId} should associate ability successfully")
    void testAddAbilityToCharacterSuccess() throws Exception {
        doNothing().when(characterService).addAbilityToCharacter(any(), eq(1L), eq(2L));

        mockMvc.perform(post("/characters/1/abilities/2").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(characterService, times(1)).addAbilityToCharacter(any(), eq(1L), eq(2L));
    }

    @Test
    @DisplayName("DELETE /characters/{id}/abilities/{abilityId} should remove ability successfully")
    void testRemoveAbilityFromCharacterSuccess() throws Exception {
        doNothing().when(characterService).removeAbilityFromCharacter(any(), eq(1L), eq(2L));

        mockMvc.perform(delete("/characters/1/abilities/2").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(characterService, times(1)).removeAbilityFromCharacter(any(), eq(1L), eq(2L));
    }
}

