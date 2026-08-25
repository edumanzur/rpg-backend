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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.DTO.CampaignMapper;
import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.DTO.CreateCampaignRequest;
import com.eduardo.rpg.Campaign.DTO.StatusTemplateMapper;
import com.eduardo.rpg.Campaign.DTO.UpdateCampaignRequest;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;

@DisplayName("CampaignService Unit Tests")
@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CampaignMapper campaignMapper;

    @Mock
    private StatusTemplateMapper statusTemplateMapper;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private StatusTemplateValidator statusTemplateValidator;

    @InjectMocks
    private CampaignService campaignService;

    private User master;
    private Campaign campaign;
    private CampaignResponseDTO campaignResponseDTO;
    private CreateCampaignRequest createCampaignRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        master = new User(1L, "masteruser", "master@example.com", "password", Role.PLAYER, null, null);
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Epic Quest");
        campaign.setDescription("A grand adventure");
        campaign.setInviteCode("ABC12345");
        campaign.setMaster(master);
        campaign.setStatus(true);
        campaignResponseDTO = new CampaignResponseDTO(1L, "Epic Quest", "A grand adventure", "ABC12345", true, 1L, "master", null, null);
        createCampaignRequest = new CreateCampaignRequest("Epic Quest", "A grand adventure", true);
        authentication = new UsernamePasswordAuthenticationToken("masteruser", "password", List.of(new SimpleGrantedAuthority("ROLE_MASTER")));
    }

    @Test
    @DisplayName("Should create campaign successfully")
    void testCreateCampaignSuccess() {
        Campaign newCampaign = new Campaign();
        newCampaign.setMaster(master);

        when(userRepository.findById(1L)).thenReturn(Optional.of(master));
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(campaignRepository.existsByNameAndMasterId("Epic Quest", 1L)).thenReturn(false);
        when(campaignMapper.toEntity(createCampaignRequest)).thenReturn(newCampaign);
        when(campaignRepository.save(any(Campaign.class))).thenReturn(campaign);
        when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponseDTO);

        CampaignResponseDTO result = campaignService.createCampaign(authentication, 1L, createCampaignRequest);

        assertNotNull(result);
        assertEquals("Epic Quest", result.name());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when master not found")
    void testCreateCampaignMasterNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);

        assertThrows(ResourceNotFoundException.class, () -> campaignService.createCampaign(authentication, 1L, createCampaignRequest));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when campaign name exists for master")
    void testCreateCampaignNameExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(master));
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(campaignRepository.existsByNameAndMasterId("Epic Quest", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> campaignService.createCampaign(authentication, 1L, createCampaignRequest));
        verify(campaignRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should find campaign by id successfully")
    void testFindCampaignByIdSuccess() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponseDTO);

        CampaignResponseDTO result = campaignService.findCampaignById(authentication, 1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Epic Quest", result.name());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when campaign not found")
    void testFindCampaignByIdNotFound() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);

        assertThrows(ResourceNotFoundException.class, () -> campaignService.findCampaignById(authentication, 1L));
    }

    @Test
    @DisplayName("Should find campaigns by master id")
    void testFindCampaignsByMasterIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(master));
        when(campaignRepository.findByMasterId(1L)).thenReturn(List.of(campaign));
        when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponseDTO);
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);

        List<CampaignResponseDTO> result = campaignService.findCampaignsByMasterId(authentication, 1L);

        assertEquals(1, result.size());
        assertEquals("Epic Quest", result.get(0).name());
    }

    @Test
    @DisplayName("Should find all campaigns")
    void testFindAllCampaignsSuccess() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        // Master should see both campaigns they own and campaigns they've joined as a player
        when(campaignRepository.findByMasterId(master.getId())).thenReturn(List.of(campaign));
        when(campaignRepository.findByPlayersContaining(master)).thenReturn(List.of());
        when(campaignMapper.toResponse(campaign)).thenReturn(campaignResponseDTO);

        Page<CampaignResponseDTO> result = campaignService.findAllCampaigns(authentication, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
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
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(campaignRepository.existsByNameAndMasterIdAndIdNot("Epic Quest Updated", 1L, 1L)).thenReturn(false);
        when(campaignMapper.toEntity(updateRequest, campaign)).thenReturn(updatedCampaign);
        when(campaignRepository.save(any(Campaign.class))).thenReturn(updatedCampaign);
        when(campaignMapper.toResponse(updatedCampaign)).thenReturn(new CampaignResponseDTO(1L, "Epic Quest Updated", "Updated adventure", "ABC12345", false, 1L, "master", null, null));

        CampaignResponseDTO result = campaignService.updateCampaign(authentication, 1L, updateRequest);

        assertNotNull(result);
        assertEquals("Epic Quest Updated", result.name());
        verify(campaignRepository, times(1)).save(any(Campaign.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating to duplicate campaign name")
    void testUpdateCampaignDuplicateName() {
        UpdateCampaignRequest updateRequest = new UpdateCampaignRequest("Another Quest", "Updated adventure", false);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(campaignRepository.existsByNameAndMasterIdAndIdNot("Another Quest", 1L, 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> campaignService.updateCampaign(authentication, 1L, updateRequest));
    }

    @Test
    @DisplayName("Should delete campaign successfully")
    void testDeleteCampaignSuccess() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);

        campaignService.deleteCampaign(authentication, 1L);

        verify(campaignRepository, times(1)).delete(campaign);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent campaign")
    void testDeleteCampaignNotFound() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);

        assertThrows(ResourceNotFoundException.class, () -> campaignService.deleteCampaign(authentication, 1L));
    }
}

