package com.eduardo.rpg.AbilitySpell.Controller;

import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellResponseDTO;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellRequest;
import com.eduardo.rpg.AbilitySpell.Service.AbilitySpellService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ability-spells")
public class AbilitySpellController {

    private final AbilitySpellService abilitySpellService;

    public AbilitySpellController(AbilitySpellService abilitySpellService) {
        this.abilitySpellService = abilitySpellService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbilitySpellResponseDTO> findAbilitySpellById(Authentication authentication, @PathVariable Long id) {
        AbilitySpellResponseDTO response = abilitySpellService.findAbilitySpellById(authentication, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<AbilitySpellResponseDTO>> findAllAbilitySpells(
            Authentication authentication,
            @RequestParam(required = false) Long campaignId,
            Pageable pageable) {
        Page<AbilitySpellResponseDTO> response = abilitySpellService.findAllAbilitySpells(authentication, campaignId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AbilitySpellResponseDTO> createAbilitySpell(Authentication authentication, @RequestBody @Valid AbilitySpellRequest dto) {
        AbilitySpellResponseDTO response = abilitySpellService.createAbilitySpell(authentication, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AbilitySpellResponseDTO> updateAbilitySpell(
        Authentication authentication,
        @PathVariable Long id,
        @RequestBody @Valid AbilitySpellRequest dto) {
        AbilitySpellResponseDTO response = abilitySpellService.updateAbilitySpell(authentication, id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAbilitySpell(Authentication authentication, @PathVariable Long id) {
        abilitySpellService.deleteAbilitySpell(authentication, id);
        return ResponseEntity.noContent().build();
    }
}

