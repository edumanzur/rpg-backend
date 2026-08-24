package com.eduardo.rpg.Note.Repository;

import com.eduardo.rpg.Note.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByCampaignId(Long campaignId);

    Optional<Note> findByIdAndCampaignId(Long id, Long campaignId);
}
