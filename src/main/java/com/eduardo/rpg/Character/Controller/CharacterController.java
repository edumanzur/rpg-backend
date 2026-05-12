package com.eduardo.rpg.Character.Controller;

import com.eduardo.rpg.Character.DTO.CharacterResponseDTO;
import com.eduardo.rpg.Character.DTO.CreateCharacterRequest;
import com.eduardo.rpg.Character.DTO.UpdateCharacterRequest;
import com.eduardo.rpg.Character.Service.CharacterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CharacterResponseDTO> findCharacterById(@PathVariable Long id) {
        CharacterResponseDTO response = characterService.findCharacterById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CharacterResponseDTO>> findAllCharacters() {
        List<CharacterResponseDTO> response = characterService.findAllCharacters();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CharacterResponseDTO>> findCharactersByUserId(@PathVariable Long userId) {
        List<CharacterResponseDTO> response = characterService.findCharactersByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<CharacterResponseDTO>> findCharactersByCampaignId(@PathVariable Long campaignId) {
        List<CharacterResponseDTO> response = characterService.findCharactersByCampaignId(campaignId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<CharacterResponseDTO> createCharacter(
        @PathVariable Long userId,
        @RequestBody @Valid CreateCharacterRequest dto) {
        CharacterResponseDTO response = characterService.createCharacter(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CharacterResponseDTO> updateCharacter(
        @PathVariable Long id,
        @RequestBody @Valid UpdateCharacterRequest dto) {
        CharacterResponseDTO response = characterService.updateCharacter(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        characterService.deleteCharacter(id);
        return ResponseEntity.noContent().build();
    }
}

