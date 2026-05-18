package com.eduardo.rpg.CharacterStatus.Controller;

import com.eduardo.rpg.CharacterStatus.DTO.CharacterStatusResponseDTO;
import com.eduardo.rpg.CharacterStatus.DTO.UpdateCharacterStatusRequest;
import com.eduardo.rpg.CharacterStatus.Service.CharacterStatusService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/characters/{characterId}/statuses")
public class CharacterStatusController {

    private final CharacterStatusService characterStatusService;

    public CharacterStatusController(CharacterStatusService characterStatusService) {
        this.characterStatusService = characterStatusService;
    }

    @GetMapping
    public ResponseEntity<List<CharacterStatusResponseDTO>> findByCharacterId(Authentication authentication, @PathVariable Long characterId) {
        return ResponseEntity.ok(characterStatusService.findStatusesByCharacterId(authentication, characterId));
    }

    @GetMapping("/{statusId}")
    public ResponseEntity<CharacterStatusResponseDTO> findById(
        Authentication authentication,
        @PathVariable Long characterId,
        @PathVariable Long statusId) {
        return ResponseEntity.ok(characterStatusService.findCharacterStatusById(authentication, characterId, statusId));
    }

    @PatchMapping("/{statusId}")
    public ResponseEntity<CharacterStatusResponseDTO> update(Authentication authentication, @PathVariable Long characterId, @PathVariable Long statusId, @RequestBody @Valid UpdateCharacterStatusRequest dto) {
        return ResponseEntity.ok(characterStatusService.updateCharacterStatus(authentication, characterId, statusId, dto));
    }
}


