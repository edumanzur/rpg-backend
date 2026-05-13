package com.eduardo.rpg.Campaign.Controller;

import com.eduardo.rpg.Campaign.DTO.CreateStatusTemplateRequest;
import com.eduardo.rpg.Campaign.DTO.StatusTemplateResponseDTO;
import com.eduardo.rpg.Campaign.DTO.UpdateStatusTemplateRequest;
import com.eduardo.rpg.Campaign.Service.StatusTemplateService;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/campaigns/{campaignId}/status-templates")
public class StatusTemplateController {

    private final StatusTemplateService statusTemplateService;

    public StatusTemplateController(StatusTemplateService statusTemplateService) {
        this.statusTemplateService = statusTemplateService;
    }

    @GetMapping
    public ResponseEntity<List<StatusTemplateResponseDTO>> findByCampaignId(Authentication authentication, @PathVariable Long campaignId) {
        return ResponseEntity.ok(statusTemplateService.findStatusTemplatesByCampaignId(authentication, campaignId));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<StatusTemplateResponseDTO> findById(Authentication authentication, @PathVariable Long campaignId, @PathVariable Long templateId) {
        return ResponseEntity.ok(statusTemplateService.findStatusTemplateById(authentication, campaignId, templateId));
    }

    @PostMapping
    public ResponseEntity<StatusTemplateResponseDTO> create(Authentication authentication, @PathVariable Long campaignId, @RequestBody @Valid CreateStatusTemplateRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(statusTemplateService.createStatusTemplate(authentication, campaignId, dto));
    }

    @PutMapping("/{templateId}")
    public ResponseEntity<StatusTemplateResponseDTO> update(Authentication authentication, @PathVariable Long campaignId, @PathVariable Long templateId, @RequestBody @Valid UpdateStatusTemplateRequest dto) {
        return ResponseEntity.ok(statusTemplateService.updateStatusTemplate(authentication, campaignId, templateId, dto));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long campaignId, @PathVariable Long templateId) {
        statusTemplateService.deleteStatusTemplate(authentication, campaignId, templateId);
        return ResponseEntity.noContent().build();
    }
}

