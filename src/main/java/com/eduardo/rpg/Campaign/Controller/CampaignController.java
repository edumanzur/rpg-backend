package com.eduardo.rpg.Campaign.Controller;

import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.DTO.CreateCampaignRequest;
import com.eduardo.rpg.Campaign.DTO.UpdateCampaignRequest;
import com.eduardo.rpg.Campaign.Service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponseDTO> findCampaignById(Authentication authentication, @PathVariable Long id) {
        CampaignResponseDTO response = campaignService.findCampaignById(authentication, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CampaignResponseDTO>> findAllCampaigns(Authentication authentication, Pageable pageable) {
        Page<CampaignResponseDTO> response = campaignService.findAllCampaigns(authentication, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/master/{masterId}")
    public ResponseEntity<java.util.List<CampaignResponseDTO>> findCampaignsByMasterId(Authentication authentication, @PathVariable Long masterId) {
        java.util.List<CampaignResponseDTO> response = campaignService.findCampaignsByMasterId(authentication, masterId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/master/{masterId}")
    public ResponseEntity<CampaignResponseDTO> createCampaign(
        Authentication authentication,
        @PathVariable Long masterId,
        @RequestBody @Valid CreateCampaignRequest dto) {
        CampaignResponseDTO response = campaignService.createCampaign(authentication, masterId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponseDTO> updateCampaign(
        Authentication authentication,
        @PathVariable Long id,
        @RequestBody @Valid UpdateCampaignRequest dto) {
        CampaignResponseDTO response = campaignService.updateCampaign(authentication, id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(Authentication authentication, @PathVariable Long id) {
        campaignService.deleteCampaign(authentication, id);
        return ResponseEntity.noContent().build();
    }
}

