package com.eduardo.rpg.Grid.Repository;

import com.eduardo.rpg.Grid.CombatGrid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CombatGridRepository extends JpaRepository<CombatGrid, Long> {

    Optional<CombatGrid> findBySessionId(Long sessionId);
}
