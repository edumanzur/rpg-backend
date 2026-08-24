package com.eduardo.rpg.Grid.Controller;

import com.eduardo.rpg.Grid.DTO.CombatGridResponseDTO;
import com.eduardo.rpg.Grid.DTO.CreateGridTokenRequest;
import com.eduardo.rpg.Grid.DTO.GridTokenResponseDTO;
import com.eduardo.rpg.Grid.DTO.UpdateGridRequest;
import com.eduardo.rpg.Grid.DTO.UpdateGridTokenRequest;
import com.eduardo.rpg.Grid.Service.CombatGridService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sessions/{sessionId}/grid")
public class CombatGridController {

    private final CombatGridService combatGridService;

    public CombatGridController(CombatGridService combatGridService) {
        this.combatGridService = combatGridService;
    }

    @GetMapping
    public ResponseEntity<CombatGridResponseDTO> getGrid(Authentication authentication, @PathVariable Long sessionId) {
        return ResponseEntity.ok(combatGridService.getGrid(authentication, sessionId));
    }

    @PutMapping
    public ResponseEntity<CombatGridResponseDTO> updateGrid(Authentication authentication, @PathVariable Long sessionId, @RequestBody @Valid UpdateGridRequest dto) {
        return ResponseEntity.ok(combatGridService.updateGrid(authentication, sessionId, dto));
    }

    @PostMapping("/tokens")
    public ResponseEntity<GridTokenResponseDTO> addToken(Authentication authentication, @PathVariable Long sessionId, @RequestBody @Valid CreateGridTokenRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(combatGridService.addToken(authentication, sessionId, dto));
    }

    @PutMapping("/tokens/{tokenId}")
    public ResponseEntity<GridTokenResponseDTO> updateToken(Authentication authentication, @PathVariable Long sessionId, @PathVariable Long tokenId, @RequestBody @Valid UpdateGridTokenRequest dto) {
        return ResponseEntity.ok(combatGridService.updateToken(authentication, sessionId, tokenId, dto));
    }

    @DeleteMapping("/tokens/{tokenId}")
    public ResponseEntity<Void> deleteToken(Authentication authentication, @PathVariable Long sessionId, @PathVariable Long tokenId) {
        combatGridService.deleteToken(authentication, sessionId, tokenId);
        return ResponseEntity.noContent().build();
    }
}
