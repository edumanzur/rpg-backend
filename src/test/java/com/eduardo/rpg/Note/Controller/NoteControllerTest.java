package com.eduardo.rpg.Note.Controller;

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

import com.eduardo.rpg.Note.DTO.NoteResponseDTO;
import com.eduardo.rpg.Note.Service.NoteService;
import com.eduardo.rpg.exception.ResourceNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("NoteController Integration Tests")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    private NoteResponseDTO noteResponseDTO;

    @BeforeEach
    void setUp() {
        noteResponseDTO = new NoteResponseDTO(1L, "Plan", "Secret content", 1L, null, 1L, null, null);
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("GET /campaigns/{campaignId}/notes should list notes")
    void testFindNotesByCampaignIdSuccess() throws Exception {
        when(noteService.findNotesByCampaignId(any(), eq(1L))).thenReturn(List.of(noteResponseDTO));

        mockMvc.perform(get("/campaigns/1/notes").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Plan")));
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("POST /campaigns/{campaignId}/notes should create a note")
    void testCreateNoteSuccess() throws Exception {
        when(noteService.createNote(any(), eq(1L), any())).thenReturn(noteResponseDTO);

        mockMvc.perform(post("/campaigns/1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Plan\",\"content\":\"Secret content\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title", is("Plan")));
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("PUT /notes/{id} should update a note")
    void testUpdateNoteSuccess() throws Exception {
        NoteResponseDTO updated = new NoteResponseDTO(1L, "Updated", "New content", 1L, null, 1L, null, null);
        when(noteService.updateNote(any(), eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/notes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated\",\"content\":\"New content\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("Updated")));
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("DELETE /notes/{id} should delete a note")
    void testDeleteNoteSuccess() throws Exception {
        doNothing().when(noteService).deleteNote(any(), eq(1L));

        mockMvc.perform(delete("/notes/1").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "masteruser", roles = "PLAYER")
    @DisplayName("PUT /notes/{id} should return 404 when note not found")
    void testUpdateNoteNotFound() throws Exception {
        when(noteService.updateNote(any(), eq(1L), any())).thenThrow(new ResourceNotFoundException("Anotação não encontrada!"));

        mockMvc.perform(put("/notes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated\",\"content\":\"New content\"}"))
            .andExpect(status().isNotFound());
    }
}
