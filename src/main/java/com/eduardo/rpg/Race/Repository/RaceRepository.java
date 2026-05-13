package com.eduardo.rpg.Race.Repository;

import com.eduardo.rpg.Race.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    Optional<Race> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}

