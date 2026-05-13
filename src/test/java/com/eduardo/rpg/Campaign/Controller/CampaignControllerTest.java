package com.eduardo.rpg.Campaign.Controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.Service.CampaignService;
import com.eduardo.rpg.exception.ResourceNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "masteruser", roles = "MASTER")
@DisplayName("CampaignController Integration Tests")
class CampaignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampaignService campaignService;

    private CampaignResponseDTO campaignResponseDTO;

    @BeforeEach
    void setUp() {
        campaignResponseDTO = new CampaignResponseDTO(1L, "Epic Quest", "A grand adventure", true, 1L, null, null);
    }

    @Test
    @DisplayName("GET /campaigns/{id} should return campaign successfully")
    void testFindCampaignByIdSuccess() throws Exception {
        when(campaignService.findCampaignById(any(), eq(1L))).thenReturn(campaignResponseDTO);

        mockMvc.perform(get("/campaigns/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Epic Quest")))
            .andExpect(jsonPath("$.masterId", is(1)));

        verify(campaignService, times(1)).findCampaignById(any(), eq(1L));
    }

    @Test
    @DisplayName("GET /campaigns/{id} should return 404 when campaign not found")
    void testFindCampaignByIdNotFound() throws Exception {
        when(campaignService.findCampaignById(any(), eq(1L)))
            .thenThrow(new ResourceNotFoundException("Campanha não encontrada!"));

        mockMvc.perform(get("/campaigns/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Campanha não encontrada!")));
    }

    @Test
    @DisplayName("GET /campaigns should return all campaigns")
    void testFindAllCampaignsSuccess() throws Exception {
        CampaignResponseDTO second = new CampaignResponseDTO(2L, "Side Quest", "Another adventure", false, 1L, null, null);
        when(campaignService.findAllCampaigns(any(), eq(PageRequest.of(0, 10))))
            .thenReturn(new PageImpl<>(List.of(campaignResponseDTO, second)));

        mockMvc.perform(get("/campaigns?page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.content[0].name", is("Epic Quest")))
            .andExpect(jsonPath("$.content[1].name", is("Side Quest")));

        verify(campaignService, times(1)).findAllCampaigns(any(), eq(PageRequest.of(0, 10)));
    }

    @Test
    @DisplayName("GET /campaigns/master/{masterId} should return campaigns by master")
    void testFindCampaignsByMasterIdSuccess() throws Exception {
        when(campaignService.findCampaignsByMasterId(any(), eq(1L))).thenReturn(List.of(campaignResponseDTO));

        mockMvc.perform(get("/campaigns/master/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].masterId", is(1)));
    }

    @Test
    @DisplayName("POST /campaigns/master/{masterId} should create campaign successfully")
    void testCreateCampaignSuccess() throws Exception {
        when(campaignService.createCampaign(any(), eq(1L), any())).thenReturn(campaignResponseDTO);

        mockMvc.perform(post("/campaigns/master/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Epic Quest",
                        "description": "A grand adventure",
                        "status": true
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Epic Quest")));

        verify(campaignService, times(1)).createCampaign(any(), eq(1L), any());
    }

    @Test
    @DisplayName("POST /campaigns/master/{masterId} should handle conflict")
    void testCreateCampaignConflict() throws Exception {
        when(campaignService.createCampaign(any(), eq(1L), any()))
            .thenThrow(new IllegalArgumentException("Já existe uma campanha com este nome para este mestre"));

        mockMvc.perform(post("/campaigns/master/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Epic Quest",
                        "description": "A grand adventure",
                        "status": true
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    @DisplayName("DELETE /campaigns/{id} should delete campaign successfully")
    void testDeleteCampaignSuccess() throws Exception {
        doNothing().when(campaignService).deleteCampaign(any(), eq(1L));

        mockMvc.perform(delete("/campaigns/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(campaignService, times(1)).deleteCampaign(any(), eq(1L));
    }

    @Test
    @DisplayName("DELETE /campaigns/{id} should return 404 when campaign not found")
    void testDeleteCampaignNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Campanha não encontrada!"))
            .when(campaignService).deleteCampaign(any(), eq(1L));

        mockMvc.perform(delete("/campaigns/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code", is("NOT_FOUND")))
            .andExpect(jsonPath("$.message", is("Campanha não encontrada!")));
    }
}


