package com.eduardo.rpg.Grid.Repository;

import com.eduardo.rpg.Grid.GridToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GridTokenRepository extends JpaRepository<GridToken, Long> {

    Optional<GridToken> findByIdAndGridId(Long id, Long gridId);
}
