package com.eduardo.rpg.Equipment.Controller;

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

import com.eduardo.rpg.Equipment.DTO.EquipmentResponseDTO;
import com.eduardo.rpg.Equipment.Service.EquipmentService;
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
@DisplayName("EquipmentController Integration Tests")
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipmentService equipmentService;

    private EquipmentResponseDTO equipmentResponseDTO;

    @BeforeEach
    void setUp() {
        equipmentResponseDTO = new EquipmentResponseDTO(
            1L,
            "Long Sword",
            "A sturdy sword",
            com.eduardo.rpg.enums.EquipmentType.WEAPON,
            "2d8",
            2,
            0,
            0,
            0,
            0,
            0,
            List.of(new EquipmentResponseDTO.RequirementDTO(1L, 1L, "Warrior", 16, 0, 0, 0, 0, 0)),
            null,
            null
        );
    }

    @Test
    @DisplayName("GET /equipments/{id} should return equipment successfully")
    void testFindEquipmentByIdSuccess() throws Exception {
        when(equipmentService.findEquipmentById(1L)).thenReturn(equipmentResponseDTO);

        mockMvc.perform(get("/equipments/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Long Sword")))
            .andExpect(jsonPath("$.type", is("WEAPON")))
            .andExpect(jsonPath("$.requirements", hasSize(1)));

        verify(equipmentService, times(1)).findEquipmentById(1L);
    }

    @Test
    @DisplayName("GET /equipments/{id} should return 404 when equipment not found")
    void testFindEquipmentByIdNotFound() throws Exception {
        when(equipmentService.findEquipmentById(1L)).thenThrow(new ResourceNotFoundException("Equipamento não encontrado!"));

        mockMvc.perform(get("/equipments/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Equipamento não encontrado!")));
    }

    @Test
    @DisplayName("GET /equipments should return all equipments")
    void testFindAllEquipmentsSuccess() throws Exception {
        EquipmentResponseDTO second = new EquipmentResponseDTO(
            2L,
            "Leather Armor",
            "Light armor",
            com.eduardo.rpg.enums.EquipmentType.ARMOR,
            null,
            0,
            1,
            2,
            0,
            0,
            0,
            List.of(),
            null,
            null
        );
        when(equipmentService.findAllEquipments(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(equipmentResponseDTO, second)));

        mockMvc.perform(get("/equipments?page=0&size=10").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].name", is("Long Sword")))
            .andExpect(jsonPath("$.content[1].name", is("Leather Armor")));

        verify(equipmentService, times(1)).findAllEquipments(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("POST /equipments should create equipment successfully")
    void testCreateEquipmentSuccess() throws Exception {
        when(equipmentService.createEquipment(any())).thenReturn(equipmentResponseDTO);

        mockMvc.perform(post("/equipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Long Sword",
                        "description": "A sturdy sword",
                        "type": "WEAPON",
                        "damage": "2d8",
                        "strengthBonus": 2,
                        "dexterityBonus": 0,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 0,
                        "charismaBonus": 0,
                        "requirements": [
                            {
                                "requiredClassId": 1,
                                "minStrength": 16,
                                "minDexterity": 0,
                                "minConstitution": 0,
                                "minIntelligence": 0,
                                "minWisdom": 0,
                                "minCharisma": 0
                            }
                        ]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Long Sword")))
            .andExpect(jsonPath("$.requirements", hasSize(1)));

        verify(equipmentService, times(1)).createEquipment(any());
    }

    @Test
    @DisplayName("POST /equipments should handle conflict")
    void testCreateEquipmentConflict() throws Exception {
        when(equipmentService.createEquipment(any())).thenThrow(new IllegalArgumentException("Já existe um equipamento com este nome"));

        mockMvc.perform(post("/equipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Long Sword",
                        "description": "A sturdy sword",
                        "type": "WEAPON",
                        "damage": "2d8",
                        "strengthBonus": 2,
                        "dexterityBonus": 0,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 0,
                        "charismaBonus": 0,
                        "requirements": []
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    @DisplayName("PUT /equipments/{id} should update equipment successfully")
    void testUpdateEquipmentSuccess() throws Exception {
        when(equipmentService.updateEquipment(eq(1L), any())).thenReturn(equipmentResponseDTO);

        mockMvc.perform(put("/equipments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Long Sword",
                        "description": "A sturdier sword",
                        "type": "WEAPON",
                        "damage": "2d10",
                        "strengthBonus": 3,
                        "dexterityBonus": 0,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 0,
                        "charismaBonus": 0,
                        "requirements": []
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Long Sword")));

        verify(equipmentService, times(1)).updateEquipment(eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /equipments/{id} should delete equipment successfully")
    void testDeleteEquipmentSuccess() throws Exception {
        doNothing().when(equipmentService).deleteEquipment(1L);

        mockMvc.perform(delete("/equipments/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(equipmentService, times(1)).deleteEquipment(1L);
    }

    @Test
    @DisplayName("DELETE /equipments/{id} should return 404 when equipment not found")
    void testDeleteEquipmentNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Equipamento não encontrado!"))
            .when(equipmentService).deleteEquipment(1L);

        mockMvc.perform(delete("/equipments/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Equipamento não encontrado!")));
    }
}

