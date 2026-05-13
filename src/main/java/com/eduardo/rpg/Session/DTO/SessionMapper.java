package com.eduardo.rpg.Session.DTO;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.Session.Session;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionMapper {

    public SessionResponseDTO toResponse(Session session) {
        if (session == null) return null;

        List<Long> characterIds = session.getCharacters() == null ? List.of() : session.getCharacters().stream()
            .map(Character::getId)
            .toList();

        return new SessionResponseDTO(
            session.getId(),
            session.getTitle(),
            session.getStory(),
            session.getNotes(),
            session.getCampaign() != null ? session.getCampaign().getId() : null,
            characterIds,
            session.getCreatedAt(),
            session.getUpdatedAt()
        );
    }

    public Session toEntity(CreateSessionRequest dto) {
        if (dto == null) return null;

        Session session = new Session();
        session.setTitle(dto.title());
        session.setStory(dto.story());
        session.setNotes(dto.notes());
        return session;
    }

    public Session toEntity(UpdateSessionRequest dto, Session session) {
        if (dto == null) return session;

        session.setTitle(dto.title());
        session.setStory(dto.story());
        session.setNotes(dto.notes());
        return session;
    }
}

