package com.eduardo.rpg.Campaign.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.DTO.CampaignMapper;
import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.DTO.CreateCampaignRequest;
import com.eduardo.rpg.Campaign.DTO.UpdateCampaignRequest;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;

@DisplayName("CampaignService Unit Tests")
@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CampaignMapper campaignMapper;

    @InjectMocks
    private CampaignService campaignService;

    private User master;
    private Campaign campaign;
    private CampaignResponseDTO campaignResponseDTO;
    private CreateCampaignRequest createCampaignRequest;

    @BeforeEach
    void setUp() {
        master = new User(1L, "masteruser", "master@example.com", "password", Role.MASTER, null, null);
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Epic Quest");
        campaign.setDescription("A grand adventure");
        campaign.setMaster(master);
        campaign.setStatus(true);
        campaignResponseDTO = new CampaignResponseDTO(1L, "Epic Quest", "A grand adventure", true, 1L, null, null);
        createCampaignRequest = new CreateCampaignRequest("Epic Quest", "A grand adventure", true);
    }

    @Test
    @DisplayName("Should create campaign successfully")
    void testCreateCampaignSuccess() {
        Campaign newCampaign = new Campaign();
        newCampaign.setMaster(master);

        when(userRepository.findById(1L)).thenReturn(Optional.of(master));
        when(campaignRepository.existsByNameAndMasterId("Epic Quest", 1L)).thenReturn(false);
        when(campaignMapper.toEntity(createCampaignRequest)).thenReturn(newCampaign);
        when(campaignRepository.save(any(Campaign.class))).thenReturn(campaign);
        when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponseDTO);

        CampaignResponseDTO result = campaignService.createCampaign(1L, createCampaignRequest);

        assertNotNull(result);
        assertEquals("Epic Quest", result.name());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when master not found")
    void testCreateCampaignMasterNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> campaignService.createCampaign(1L, createCampaignRequest));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when campaign name exists for master")
    void testCreateCampaignNameExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(master));
        when(campaignRepository.existsByNameAndMasterId("Epic Quest", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> campaignService.createCampaign(1L, createCampaignRequest));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find campaign by id successfully")
    void testFindCampaignByIdSuccess() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponseDTO);

        CampaignResponseDTO result = campaignService.findCampaignById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Epic Quest", result.name());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when campaign not found")
    void testFindCampaignByIdNotFound() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> campaignService.findCampaignById(1L));
    }

    @Test
    @DisplayName("Should find campaigns by master id")
    void testFindCampaignsByMasterIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(master));
        when(campaignRepository.findByMasterId(1L)).thenReturn(List.of(campaign));
        when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponseDTO);

        List<CampaignResponseDTO> result = campaignService.findCampaignsByMasterId(1L);

        assertEquals(1, result.size());
        assertEquals("Epic Quest", result.get(0).name());
    }

    @Test
    @DisplayName("Should find all campaigns")
    void testFindAllCampaignsSuccess() {
        when(campaignRepository.findAll()).thenReturn(List.of(campaign));
        when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponseDTO);

        List<CampaignResponseDTO> result = campaignService.findAllCampaigns();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should update campaign successfully")
    void testUpdateCampaignSuccess() {
        UpdateCampaignRequest updateRequest = new UpdateCampaignRequest("Epic Quest Updated", "Updated adventure", false);
        Campaign updatedCampaign = new Campaign();
        updatedCampaign.setId(1L);
        updatedCampaign.setName("Epic Quest Updated");
        updatedCampaign.setDescription("Updated adventure");
        updatedCampaign.setMaster(master);
        updatedCampaign.setStatus(false);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.existsByNameAndMasterIdAndIdNot("Epic Quest Updated", 1L, 1L)).thenReturn(false);
        when(campaignMapper.toEntity(updateRequest, campaign)).thenReturn(updatedCampaign);
        when(campaignRepository.save(any(Campaign.class))).thenReturn(updatedCampaign);
        when(campaignMapper.toResponse(updatedCampaign)).thenReturn(new CampaignResponseDTO(1L, "Epic Quest Updated", "Updated adventure", false, 1L, null, null));

        CampaignResponseDTO result = campaignService.updateCampaign(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Epic Quest Updated", result.name());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating to duplicate campaign name")
    void testUpdateCampaignDuplicateName() {
        UpdateCampaignRequest updateRequest = new UpdateCampaignRequest("Another Quest", "Updated adventure", false);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepository.existsByNameAndMasterIdAndIdNot("Another Quest", 1L, 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> campaignService.updateCampaign(1L, updateRequest));
    }

    @Test
    @DisplayName("Should delete campaign successfully")
    void testDeleteCampaignSuccess() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        campaignService.deleteCampaign(1L);

        verify(campaignRepository, times(1)).delete(campaign);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent campaign")
    void testDeleteCampaignNotFound() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> campaignService.deleteCampaign(1L));
    }
}

