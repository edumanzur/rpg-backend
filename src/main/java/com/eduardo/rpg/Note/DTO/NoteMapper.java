package com.eduardo.rpg.Note.DTO;

import com.eduardo.rpg.Note.Note;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteResponseDTO toResponse(Note note) {
        if (note == null) return null;

        return new NoteResponseDTO(
            note.getId(),
            note.getTitle(),
            note.getContent(),
            note.getCampaign() != null ? note.getCampaign().getId() : null,
            note.getSession() != null ? note.getSession().getId() : null,
            note.getAuthor() != null ? note.getAuthor().getId() : null,
            note.getCreatedAt(),
            note.getUpdatedAt()
        );
    }
}
