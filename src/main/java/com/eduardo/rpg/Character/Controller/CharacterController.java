package com.eduardo.rpg.Character.Controller;

import com.eduardo.rpg.Character.DTO.CharacterResponseDTO;
import com.eduardo.rpg.Character.DTO.CreateCharacterRequest;
import com.eduardo.rpg.Character.DTO.UpdateCharacterRequest;
import com.eduardo.rpg.Character.Service.CharacterService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharacterResponseDTO> findCharacterById(Authentication authentication, @PathVariable Long id) {
        CharacterResponseDTO response = characterService.findCharacterById(authentication, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CharacterResponseDTO>> findAllCharacters(Authentication authentication, Pageable pageable) {
        Page<CharacterResponseDTO> response = characterService.findAllCharacters(authentication, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CharacterResponseDTO>> findCharactersByUserId(Authentication authentication, @PathVariable Long userId) {
        List<CharacterResponseDTO> response = characterService.findCharactersByUserId(authentication, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<CharacterResponseDTO>> findCharactersByCampaignId(Authentication authentication, @PathVariable Long campaignId) {
        List<CharacterResponseDTO> response = characterService.findCharactersByCampaignId(authentication, campaignId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<CharacterResponseDTO> createCharacter(
        Authentication authentication,
        @PathVariable Long userId,
        @RequestBody @Valid CreateCharacterRequest dto) {
        CharacterResponseDTO response = characterService.createCharacter(authentication, userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterResponseDTO> updateCharacter(
        Authentication authentication,
        @PathVariable Long id,
        @RequestBody @Valid UpdateCharacterRequest dto) {
        CharacterResponseDTO response = characterService.updateCharacter(authentication, id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(Authentication authentication, @PathVariable Long id) {
        characterService.deleteCharacter(authentication, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/equipments/{equipmentId}")
    public ResponseEntity<Void> addEquipmentToCharacter(
        Authentication authentication,
        @PathVariable Long id,
        @PathVariable Long equipmentId) {
        characterService.addEquipmentToCharacter(authentication, id, equipmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/equipments/{equipmentId}")
    public ResponseEntity<Void> removeEquipmentFromCharacter(
        Authentication authentication,
        @PathVariable Long id,
        @PathVariable Long equipmentId) {
        characterService.removeEquipmentFromCharacter(authentication, id, equipmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/abilities/{abilityId}")
    public ResponseEntity<Void> addAbilityToCharacter(
        Authentication authentication,
        @PathVariable Long id,
        @PathVariable Long abilityId) {
        characterService.addAbilityToCharacter(authentication, id, abilityId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/abilities/{abilityId}")
    public ResponseEntity<Void> removeAbilityFromCharacter(
        Authentication authentication,
        @PathVariable Long id,
        @PathVariable Long abilityId) {
        characterService.removeAbilityFromCharacter(authentication, id, abilityId);
        return ResponseEntity.noContent().build();
    }
}

