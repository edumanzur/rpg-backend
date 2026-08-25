package com.eduardo.rpg.Grid;

import static org.junit.jupiter.api.Assertions.*;

import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.DTO.CreateCampaignRequest;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.Campaign.Service.CampaignService;
import com.eduardo.rpg.Grid.DTO.CombatGridResponseDTO;
import com.eduardo.rpg.Grid.DTO.CreateGridTokenRequest;
import com.eduardo.rpg.Grid.DTO.UpdateGridTokenRequest;
import com.eduardo.rpg.Grid.Service.CombatGridService;
import com.eduardo.rpg.Note.DTO.CreateNoteRequest;
import com.eduardo.rpg.Note.DTO.NoteResponseDTO;
import com.eduardo.rpg.Note.Service.NoteService;
import com.eduardo.rpg.Session.DTO.CreateSessionRequest;
import com.eduardo.rpg.Session.DTO.SessionResponseDTO;
import com.eduardo.rpg.Session.Service.SessionService;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class GridAndNoteAuthorizationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CombatGridService combatGridService;

    @Autowired
    private NoteService noteService;

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("secret");
        user.setRole(Role.PLAYER);
        return userRepository.save(user);
    }

    private Authentication authFor(User user) {
        return new UsernamePasswordAuthenticationToken(user.getUsername(), "secret", List.of(new SimpleGrantedAuthority("ROLE_PLAYER")));
    }

    @Test
    void randomPlayerCanReadButNotWriteGridOrNotes() {
        User master = createUser("grid-master");
        User outsider = createUser("grid-outsider");

        Authentication masterAuth = authFor(master);
        Authentication outsiderAuth = authFor(outsider);

        CampaignResponseDTO campaign = campaignService.createCampaign(
            masterAuth, master.getId(), new CreateCampaignRequest("Grid Campaign", "For grid/notes auth test", true));

        // outsider joins as a regular campaign player (not master) -- this is the
        // "random PLAYER account" the read-gating (requireCampaignViewPermission)
        // should allow to read, but not write
        campaignService.joinCampaign(outsiderAuth, campaign.inviteCode());

        SessionResponseDTO session = sessionService.createSession(masterAuth,
            new CreateSessionRequest("Session 1", "The story begins", null, campaign.id(), List.of()));

        // A regular campaign player must be able to read a session and the
        // campaign's session list (findSessionById / findSessionsByCampaignId
        // used to require *campaign ownership*, not just view access, so a
        // real player got 403 opening any session at all — caught via
        // manual end-to-end testing of the session detail page, not by the
        // pre-existing test suite, since those tests fully mock
        // AccessControlService and never exercise the real permission call).
        SessionResponseDTO readById = sessionService.findSessionById(outsiderAuth, session.id());
        assertEquals(session.id(), readById.id());

        List<SessionResponseDTO> campaignSessions = sessionService.findSessionsByCampaignId(outsiderAuth, campaign.id());
        assertEquals(1, campaignSessions.size());
        assertEquals(session.id(), campaignSessions.get(0).id());

        // but still can't write to it
        assertThrows(AccessDeniedException.class, () -> sessionService.deleteSession(outsiderAuth, session.id()));

        // outsider (not master, not a campaign player) can read the auto-created grid
        CombatGridResponseDTO grid = combatGridService.getGrid(outsiderAuth, session.id());
        assertNotNull(grid);
        assertEquals(20, grid.rows());
        assertEquals(20, grid.cols());

        // but cannot add a token
        assertThrows(AccessDeniedException.class, () -> combatGridService.addToken(
            outsiderAuth, session.id(), new CreateGridTokenRequest(1, 1, "Goblin", "#ff0000", null)));

        // master adds a token, outsider still can't move it
        var token = combatGridService.addToken(masterAuth, session.id(), new CreateGridTokenRequest(1, 1, "Goblin", "#ff0000", null));
        assertThrows(AccessDeniedException.class, () -> combatGridService.updateToken(
            outsiderAuth, session.id(), token.id(), new UpdateGridTokenRequest(2, 2, "Goblin", "#ff0000", null)));

        // outsider can read campaign notes (empty list, no exception)
        List<NoteResponseDTO> notes = noteService.findNotesByCampaignId(outsiderAuth, campaign.id());
        assertTrue(notes.isEmpty());

        // but cannot create a note in someone else's campaign
        assertThrows(AccessDeniedException.class, () -> noteService.createNote(
            outsiderAuth, campaign.id(), new CreateNoteRequest("Secret plan", "The dragon sleeps at noon", null)));

        // master can create a note, and it's visible to the outsider on read
        NoteResponseDTO note = noteService.createNote(masterAuth, campaign.id(), new CreateNoteRequest("Secret plan", "The dragon sleeps at noon", null));
        List<NoteResponseDTO> notesAfter = noteService.findNotesByCampaignId(outsiderAuth, campaign.id());
        assertEquals(1, notesAfter.size());
        assertEquals(note.id(), notesAfter.get(0).id());
    }
}
