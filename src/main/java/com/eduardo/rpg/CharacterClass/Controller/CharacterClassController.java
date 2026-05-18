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
    public ResponseEntity<Page<CharacterClassResponseDTO>> findAllCharacterClasses(Pageable pageable) {
        Page<CharacterClassResponseDTO> response = characterClassService.findAllCharacterClasses(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<CharacterClassResponseDTO> createCharacterClass(@RequestBody @Valid CreateCharacterClassRequest dto) {
        CharacterClassResponseDTO response = characterClassService.createCharacterClass(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<CharacterClassResponseDTO> updateCharacterClass(
        @PathVariable Long id,
        @RequestBody @Valid UpdateCharacterClassRequest dto) {
        CharacterClassResponseDTO response = characterClassService.updateCharacterClass(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<Void> deleteCharacterClass(@PathVariable Long id) {
        characterClassService.deleteCharacterClass(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/abilities/{abilityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MASTER')")
    public ResponseEntity<Void> addAbilityToClass(@PathVariable Long id, @PathVariable Long abilityId) {
        characterClassService.addAbilityToClass(id, abilityId);
        return ResponseEntity.noContent().build();
    }
}

