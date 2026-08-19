package com.eduardo.rpg.CharacterClass.Controller;

import com.eduardo.rpg.CharacterClass.DTO.CharacterClassResponseDTO;
import com.eduardo.rpg.CharacterClass.DTO.CreateCharacterClassRequest;
import com.eduardo.rpg.CharacterClass.DTO.UpdateCharacterClassRequest;
import com.eduardo.rpg.CharacterClass.Service.CharacterClassService;
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
@RequestMapping("/character-classes")
public class CharacterClassController {

    private final CharacterClassService characterClassService;

    public CharacterClassController(CharacterClassService characterClassService) {
        this.characterClassService = characterClassService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharacterClassResponseDTO> findCharacterClassById(@PathVariable Long id) {
        CharacterClassResponseDTO response = characterClassService.findCharacterClassById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CharacterClassResponseDTO>> findAllCharacterClasses(
            @RequestParam(required = false) Long campaignId,
            Pageable pageable) {
        Page<CharacterClassResponseDTO> response = characterClassService.findAllCharacterClasses(campaignId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CharacterClassResponseDTO> createCharacterClass(Authentication authentication, @RequestBody @Valid CreateCharacterClassRequest dto) {
        CharacterClassResponseDTO response = characterClassService.createCharacterClass(authentication, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterClassResponseDTO> updateCharacterClass(
        Authentication authentication,
        @PathVariable Long id,
        @RequestBody @Valid UpdateCharacterClassRequest dto) {
        CharacterClassResponseDTO response = characterClassService.updateCharacterClass(authentication, id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacterClass(Authentication authentication, @PathVariable Long id) {
        characterClassService.deleteCharacterClass(authentication, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/abilities/{abilityId}")
    public ResponseEntity<Void> addAbilityToClass(Authentication authentication, @PathVariable Long id, @PathVariable Long abilityId) {
        characterClassService.addAbilityToClass(authentication, id, abilityId);
        return ResponseEntity.noContent().build();
    }
}

