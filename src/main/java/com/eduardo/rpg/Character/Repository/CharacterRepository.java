package com.eduardo.rpg.Character.Repository;

import com.eduardo.rpg.Character.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findByUserId(Long userId);
    List<Character> findByCampaignId(Long campaignId);
    boolean existsByNameAndUserId(String name, Long userId);
}

