package com.eduardo.rpg.Campaign.Controller;

import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.DTO.CreateCampaignRequest;
import com.eduardo.rpg.Campaign.DTO.UpdateCampaignRequest;
import com.eduardo.rpg.Campaign.Service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponseDTO> findCampaignById(@PathVariable Long id) {
        CampaignResponseDTO response = campaignService.findCampaignById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CampaignResponseDTO>> findAllCampaigns() {
        List<CampaignResponseDTO> response = campaignService.findAllCampaigns();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/master/{masterId}")
    public ResponseEntity<List<CampaignResponseDTO>> findCampaignsByMasterId(@PathVariable Long masterId) {
        List<CampaignResponseDTO> response = campaignService.findCampaignsByMasterId(masterId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/master/{masterId}")
    public ResponseEntity<CampaignResponseDTO> createCampaign(
        @PathVariable Long masterId,
        @RequestBody @Valid CreateCampaignRequest dto) {
        CampaignResponseDTO response = campaignService.createCampaign(masterId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponseDTO> updateCampaign(
        @PathVariable Long id,
        @RequestBody @Valid UpdateCampaignRequest dto) {
        CampaignResponseDTO response = campaignService.updateCampaign(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }
}

