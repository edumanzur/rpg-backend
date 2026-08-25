package com.eduardo.rpg.Note.Service;

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
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final CampaignRepository campaignRepository;
    private final SessionRepository sessionRepository;
    private final NoteMapper noteMapper;
    private final AccessControlService accessControlService;

    // Every note is private to its author — including the master's own
    // (this is a personal-journal feature, not a GM handout tool). Reads
    // filter to authorId == the requester; anyone with campaign access can
    // write their own notes, but only the author (or ADMIN) can edit/delete
    // one, checked via requireSameUserOrAdmin against the note's authorId
    // rather than campaign ownership.
    @Transactional(readOnly = true)
    public List<NoteResponseDTO> findNotesByCampaignId(Authentication authentication, Long campaignId) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = getCampaignForRead(authUser, campaignId);

        return noteRepository.findByCampaignIdAndAuthorId(campaign.getId(), authUser.getId())
            .stream()
            .map(noteMapper::toResponse)
            .toList();
    }

    @Transactional
    public NoteResponseDTO createNote(Authentication authentication, Long campaignId, CreateNoteRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Campaign campaign = getCampaignForRead(authUser, campaignId);

        Note note = new Note();
        note.setTitle(dto.title());
        note.setContent(dto.content());
        note.setCampaign(campaign);
        note.setSession(resolveSession(dto.sessionId(), campaign.getId()));
        note.setAuthor(authUser);

        Note saved = noteRepository.save(note);
        return noteMapper.toResponse(saved);
    }

    @Transactional
    public NoteResponseDTO updateNote(Authentication authentication, Long id, UpdateNoteRequest dto) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Note note = noteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Anotação não encontrada!"));

        accessControlService.requireSameUserOrAdmin(authUser, note.getAuthor() != null ? note.getAuthor().getId() : null);

        note.setTitle(dto.title());
        note.setContent(dto.content());
        note.setSession(resolveSession(dto.sessionId(), note.getCampaign().getId()));

        Note saved = noteRepository.save(note);
        return noteMapper.toResponse(saved);
    }

    @Transactional
    public void deleteNote(Authentication authentication, Long id) {
        User authUser = accessControlService.getAuthenticatedUser(authentication);
        Note note = noteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Anotação não encontrada!"));

        accessControlService.requireSameUserOrAdmin(authUser, note.getAuthor() != null ? note.getAuthor().getId() : null);
        noteRepository.delete(note);
    }

    private Session resolveSession(Long sessionId, Long campaignId) {
        if (sessionId == null) return null;

        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada!"));

        if (session.getCampaign() == null || !campaignId.equals(session.getCampaign().getId())) {
            throw new IllegalArgumentException("A sessão informada não pertence a esta campanha");
        }

        return session;
    }

    private Campaign getCampaignForRead(User authUser, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        accessControlService.requireCampaignViewPermission(authUser, campaign);
        return campaign;
    }

}
