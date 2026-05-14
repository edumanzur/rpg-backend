package com.eduardo.rpg.CharacterStatus.Repository;

import com.eduardo.rpg.CharacterStatus.CharacterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterStatusRepository extends JpaRepository<CharacterStatus, Long> {
    @SuppressWarnings("unused")
    @Query("select cs from CharacterStatus cs where cs.character.id = :characterId")
    List<CharacterStatus> findByCharacterId(@Param("characterId") Long characterId);

    @Query("select cs from CharacterStatus cs where cs.character.id = :characterId and cs.template.id = :templateId")
    @SuppressWarnings("unused")
    Optional<CharacterStatus> findByCharacterIdAndTemplateId(@Param("characterId") Long characterId, @Param("templateId") Long templateId);

    @Modifying
    @Transactional
    @Query("delete from CharacterStatus cs where cs.template.id = :templateId")
    void deleteByTemplateId(@Param("templateId") Long templateId);
}

