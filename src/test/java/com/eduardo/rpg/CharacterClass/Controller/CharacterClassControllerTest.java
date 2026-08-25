package com.eduardo.rpg.CharacterClass.Controller;

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

import com.eduardo.rpg.CharacterClass.DTO.CharacterClassResponseDTO;
import com.eduardo.rpg.CharacterClass.Service.CharacterClassService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CharacterClassController Integration Tests")
class CharacterClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CharacterClassService characterClassService;

    private CharacterClassResponseDTO characterClassResponseDTO;

    @BeforeEach
    void setUp() {
        characterClassResponseDTO = new CharacterClassResponseDTO(1L, "Ranger", "Skilled wilderness fighter", 1, 2, 0, 0, 1, 0, null, 0, null, null, null);
    }

    @Test
    @DisplayName("GET /character-classes/{id} should return class successfully")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testFindCharacterClassByIdSuccess() throws Exception {
        when(characterClassService.findCharacterClassById(1L)).thenReturn(characterClassResponseDTO);

        mockMvc.perform(get("/character-classes/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Ranger")))
            .andExpect(jsonPath("$.dexterityBonus", is(2)));

        verify(characterClassService, times(1)).findCharacterClassById(1L);
    }

    @Test
    @DisplayName("GET /character-classes/{id} should return 404 when class not found")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testFindCharacterClassByIdNotFound() throws Exception {
        when(characterClassService.findCharacterClassById(1L)).thenThrow(new ResourceNotFoundException("Classe não encontrada!"));

        mockMvc.perform(get("/character-classes/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Classe não encontrada!")));
    }

    @Test
    @DisplayName("GET /character-classes should return all classes")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testFindAllCharacterClassesSuccess() throws Exception {
        CharacterClassResponseDTO second = new CharacterClassResponseDTO(2L, "Mage", "Arcane specialist", 0, 0, 0, 2, 1, 0, null, 0, null, null, null);
        when(characterClassService.findAllCharacterClasses(eq((Long) null), eq(PageRequest.of(0, 10)))).thenReturn(new PageImpl<>(List.of(characterClassResponseDTO, second)));

        mockMvc.perform(get("/character-classes?page=0&size=10").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].name", is("Ranger")))
            .andExpect(jsonPath("$.content[1].name", is("Mage")));

        verify(characterClassService, times(1)).findAllCharacterClasses(eq((Long) null), eq(PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("POST /character-classes should create class successfully")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateCharacterClassSuccess() throws Exception {
        when(characterClassService.createCharacterClass(any(Authentication.class), any())).thenReturn(characterClassResponseDTO);

        mockMvc.perform(post("/character-classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Ranger",
                        "description": "Skilled wilderness fighter",
                        "strengthBonus": 1,
                        "dexterityBonus": 2,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 1,
                        "charismaBonus": 0
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Ranger")));

        verify(characterClassService, times(1)).createCharacterClass(any(Authentication.class), any());
    }

    @Test
    @DisplayName("POST /character-classes should handle conflict")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCreateCharacterClassConflict() throws Exception {
        when(characterClassService.createCharacterClass(any(Authentication.class), any())).thenThrow(new IllegalArgumentException("Já existe uma classe com este nome"));

        mockMvc.perform(post("/character-classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Ranger",
                        "description": "Skilled wilderness fighter",
                        "strengthBonus": 1,
                        "dexterityBonus": 2,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 1,
                        "charismaBonus": 0
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    @DisplayName("PUT /character-classes/{id} should update class successfully")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateCharacterClassSuccess() throws Exception {
        when(characterClassService.updateCharacterClass(any(Authentication.class), eq(1L), any())).thenReturn(characterClassResponseDTO);

        mockMvc.perform(put("/character-classes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Ranger",
                        "description": "Skilled wilderness fighter",
                        "strengthBonus": 1,
                        "dexterityBonus": 3,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 1,
                        "charismaBonus": 0
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("Ranger")));

        verify(characterClassService, times(1)).updateCharacterClass(any(Authentication.class), eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /character-classes/{id} should delete class successfully")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteCharacterClassSuccess() throws Exception {
        doNothing().when(characterClassService).deleteCharacterClass(any(Authentication.class), eq(1L));

        mockMvc.perform(delete("/character-classes/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(characterClassService, times(1)).deleteCharacterClass(any(Authentication.class), eq(1L));
    }

    @Test
    @DisplayName("DELETE /character-classes/{id} should return 404 when class not found")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteCharacterClassNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Classe não encontrada!"))
            .when(characterClassService).deleteCharacterClass(any(Authentication.class), eq(1L));

        mockMvc.perform(delete("/character-classes/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Classe não encontrada!")));
    }

    @Test
    @DisplayName("POST /character-classes/{id}/abilities/{abilityId} should associate ability successfully")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testAddAbilityToClassSuccess() throws Exception {
        doNothing().when(characterClassService).addAbilityToClass(any(Authentication.class), eq(1L), eq(1L));

        mockMvc.perform(post("/character-classes/1/abilities/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(characterClassService, times(1)).addAbilityToClass(any(Authentication.class), eq(1L), eq(1L));
    }

    @Test
    @DisplayName("POST /character-classes should return 403 when user has PLAYER role")
    @WithMockUser(username = "player", roles = "PLAYER")
    void testCreateCharacterClassWithPlayerRoleShouldForbidden() throws Exception {
        when(characterClassService.createCharacterClass(any(Authentication.class), any()))
            .thenThrow(new org.springframework.security.access.AccessDeniedException("Apenas ADMIN pode gerenciar conteúdo global"));

        mockMvc.perform(post("/character-classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Ranger",
                        "description": "Skilled wilderness fighter",
                        "strengthBonus": 1,
                        "dexterityBonus": 2,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 1,
                        "charismaBonus": 0
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /character-classes/{id} should return 403 when user has PLAYER role")
    @WithMockUser(username = "player", roles = "PLAYER")
    void testUpdateCharacterClassWithPlayerRoleShouldForbidden() throws Exception {
        when(characterClassService.updateCharacterClass(any(Authentication.class), eq(1L), any()))
            .thenThrow(new org.springframework.security.access.AccessDeniedException("Apenas ADMIN pode gerenciar conteúdo global"));

        mockMvc.perform(put("/character-classes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Ranger",
                        "description": "Skilled wilderness fighter",
                        "strengthBonus": 1,
                        "dexterityBonus": 3,
                        "constitutionBonus": 0,
                        "intelligenceBonus": 0,
                        "wisdomBonus": 1,
                        "charismaBonus": 0
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /character-classes/{id} should return 403 when user has PLAYER role")
    @WithMockUser(username = "player", roles = "PLAYER")
    void testDeleteCharacterClassWithPlayerRoleShouldForbidden() throws Exception {
        doThrow(new org.springframework.security.access.AccessDeniedException("Apenas ADMIN pode gerenciar conteúdo global"))
            .when(characterClassService).deleteCharacterClass(any(Authentication.class), eq(1L));

        mockMvc.perform(delete("/character-classes/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }
}

