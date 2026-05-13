package com.eduardo.rpg.CharacterClass.Repository;

import com.eduardo.rpg.CharacterClass.CharacterClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterClassRepository extends JpaRepository<CharacterClass, Long> {
    Optional<CharacterClass> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}

