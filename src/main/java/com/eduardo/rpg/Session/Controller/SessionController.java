package com.eduardo.rpg.Session.Controller;

import com.eduardo.rpg.Session.DTO.CreateSessionRequest;
import com.eduardo.rpg.Session.DTO.SessionResponseDTO;
import com.eduardo.rpg.Session.DTO.UpdateSessionRequest;
import com.eduardo.rpg.Session.Service.SessionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponseDTO> findSessionById(Authentication authentication, @PathVariable Long id) {
        SessionResponseDTO response = sessionService.findSessionById(authentication, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<SessionResponseDTO>> findAllSessions(Authentication authentication, Pageable pageable) {
        Page<SessionResponseDTO> response = sessionService.findAllSessions(authentication, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<SessionResponseDTO>> findSessionsByCampaignId(Authentication authentication, @PathVariable Long campaignId) {
        List<SessionResponseDTO> response = sessionService.findSessionsByCampaignId(authentication, campaignId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SessionResponseDTO> createSession(Authentication authentication, @RequestBody @Valid CreateSessionRequest dto) {
        SessionResponseDTO response = sessionService.createSession(authentication, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionResponseDTO> updateSession(Authentication authentication, @PathVariable Long id, @RequestBody @Valid UpdateSessionRequest dto) {
        SessionResponseDTO response = sessionService.updateSession(authentication, id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(Authentication authentication, @PathVariable Long id) {
        sessionService.deleteSession(authentication, id);
        return ResponseEntity.noContent().build();
    }
}

