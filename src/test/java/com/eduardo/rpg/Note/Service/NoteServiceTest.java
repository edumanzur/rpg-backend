package com.eduardo.rpg.Note.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.Note.DTO.CreateNoteRequest;
import com.eduardo.rpg.Note.DTO.NoteMapper;
import com.eduardo.rpg.Note.DTO.NoteResponseDTO;
import com.eduardo.rpg.Note.DTO.UpdateNoteRequest;
import com.eduardo.rpg.Note.Note;
import com.eduardo.rpg.Note.Repository.NoteRepository;
import com.eduardo.rpg.Session.Repository.SessionRepository;
import com.eduardo.rpg.Session.Session;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;

@DisplayName("NoteService Unit Tests")
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private NoteMapper noteMapper;

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private NoteService noteService;

    private User master;
    private Campaign campaign;
    private Note note;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        master = new User(1L, "masteruser", "master@example.com", "password", Role.PLAYER, null, null);
        campaign = new Campaign();
        campaign.setId(1L);
        campaign.setName("Epic Quest");
        campaign.setMaster(master);

        note = new Note();
        note.setId(1L);
        note.setTitle("Plan");
        note.setContent("Secret content");
        note.setCampaign(campaign);

        authentication = new UsernamePasswordAuthenticationToken("masteruser", "password", List.of(new SimpleGrantedAuthority("ROLE_PLAYER")));
    }

    @Test
    @DisplayName("Should list notes for a campaign")
    void testFindNotesByCampaignId() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(noteRepository.findByCampaignId(1L)).thenReturn(List.of(note));
        when(noteMapper.toResponse(note)).thenReturn(new NoteResponseDTO(1L, "Plan", "Secret content", 1L, null, null, null));

        List<NoteResponseDTO> result = noteService.findNotesByCampaignId(authentication, 1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should create a campaign-wide note")
    void testCreateNoteCampaignWide() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(noteRepository.save(any(Note.class))).thenReturn(note);
        when(noteMapper.toResponse(note)).thenReturn(new NoteResponseDTO(1L, "Plan", "Secret content", 1L, null, null, null));

        NoteResponseDTO result = noteService.createNote(authentication, 1L, new CreateNoteRequest("Plan", "Secret content", null));

        assertNotNull(result);
        assertNull(result.sessionId());
    }

    @Test
    @DisplayName("Should reject a session that doesn't belong to the campaign")
    void testCreateNoteInvalidSession() {
        Campaign otherCampaign = new Campaign();
        otherCampaign.setId(2L);

        Session foreignSession = new Session();
        foreignSession.setId(9L);
        foreignSession.setCampaign(otherCampaign);

        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(sessionRepository.findById(9L)).thenReturn(Optional.of(foreignSession));

        assertThrows(IllegalArgumentException.class, () -> noteService.createNote(
            authentication, 1L, new CreateNoteRequest("Plan", "Secret content", 9L)));
    }

    @Test
    @DisplayName("Should deny note creation for non-master")
    void testCreateNoteDenied() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        doThrow(new AccessDeniedException("Sem permissão")).when(accessControlService).requireCampaignOwnerOrAdmin(master, campaign);

        assertThrows(AccessDeniedException.class, () -> noteService.createNote(
            authentication, 1L, new CreateNoteRequest("Plan", "Secret content", null)));
    }

    @Test
    @DisplayName("Should update a note")
    void testUpdateNote() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenReturn(note);
        when(noteMapper.toResponse(note)).thenReturn(new NoteResponseDTO(1L, "Updated", "New content", 1L, null, null, null));

        NoteResponseDTO result = noteService.updateNote(authentication, 1L, new UpdateNoteRequest("Updated", "New content", null));

        assertEquals("Updated", result.title());
    }

    @Test
    @DisplayName("Should delete a note")
    void testDeleteNote() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        noteService.deleteNote(authentication, 1L);

        verify(noteRepository, times(1)).delete(note);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when note doesn't exist")
    void testUpdateNoteNotFound() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(master);
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.updateNote(
            authentication, 1L, new UpdateNoteRequest("Updated", "New content", null)));
    }
}
