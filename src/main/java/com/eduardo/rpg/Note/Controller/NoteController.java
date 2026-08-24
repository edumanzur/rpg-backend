package com.eduardo.rpg.Note.Controller;

import com.eduardo.rpg.Note.DTO.CreateNoteRequest;
import com.eduardo.rpg.Note.DTO.NoteResponseDTO;
import com.eduardo.rpg.Note.DTO.UpdateNoteRequest;
import com.eduardo.rpg.Note.Service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/campaigns/{campaignId}/notes")
    public ResponseEntity<List<NoteResponseDTO>> findNotesByCampaignId(Authentication authentication, @PathVariable Long campaignId) {
        return ResponseEntity.ok(noteService.findNotesByCampaignId(authentication, campaignId));
    }

    @PostMapping("/campaigns/{campaignId}/notes")
    public ResponseEntity<NoteResponseDTO> createNote(Authentication authentication, @PathVariable Long campaignId, @RequestBody @Valid CreateNoteRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.createNote(authentication, campaignId, dto));
    }

    @PutMapping("/notes/{id}")
    public ResponseEntity<NoteResponseDTO> updateNote(Authentication authentication, @PathVariable Long id, @RequestBody @Valid UpdateNoteRequest dto) {
        return ResponseEntity.ok(noteService.updateNote(authentication, id, dto));
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNote(Authentication authentication, @PathVariable Long id) {
        noteService.deleteNote(authentication, id);
        return ResponseEntity.noContent().build();
    }
}
