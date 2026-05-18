package com.eduardo.rpg.AbilitySpell.Controller;

import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellResponseDTO;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellRequest;
import com.eduardo.rpg.AbilitySpell.Service.AbilitySpellService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ability-spells")
public class AbilitySpellController {

    private final AbilitySpellService abilitySpellService;

    public AbilitySpellController(AbilitySpellService abilitySpellService) {
        this.abilitySpellService = abilitySpellService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbilitySpellResponseDTO> findAbilitySpellById(@PathVariable Long id) {
        AbilitySpellResponseDTO response = abilitySpellService.findAbilitySpellById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<AbilitySpellResponseDTO>> findAllAbilitySpells(Pageable pageable) {
        Page<AbilitySpellResponseDTO> response = abilitySpellService.findAllAbilitySpells(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<AbilitySpellResponseDTO> createAbilitySpell(@RequestBody @Valid AbilitySpellRequest dto) {
        AbilitySpellResponseDTO response = abilitySpellService.createAbilitySpell(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<AbilitySpellResponseDTO> updateAbilitySpell(
        @PathVariable Long id,
        @RequestBody @Valid AbilitySpellRequest dto) {
        AbilitySpellResponseDTO response = abilitySpellService.updateAbilitySpell(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<Void> deleteAbilitySpell(@PathVariable Long id) {
        abilitySpellService.deleteAbilitySpell(id);
        return ResponseEntity.noContent().build();
    }
}

