package com.eduardo.rpg.CharacterStatus.Repository;

import com.eduardo.rpg.CharacterStatus.CharacterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterStatusRepository extends JpaRepository<CharacterStatus, Long> {
    List<CharacterStatus> findByCharacterId(Long characterId);
    List<CharacterStatus> findByTemplateId(Long templateId);
    Optional<CharacterStatus> findByIdAndCharacterId(Long id, Long characterId);
    Optional<CharacterStatus> findByCharacterIdAndTemplateId(Long characterId, Long templateId);
    void deleteByTemplateId(Long templateId);
}

