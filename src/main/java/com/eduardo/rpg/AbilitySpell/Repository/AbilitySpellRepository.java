package com.eduardo.rpg.AbilitySpell.Repository;

import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AbilitySpellRepository extends JpaRepository<AbilitySpell, Long> {
    Optional<AbilitySpell> findByNameIgnoreCase(String name);
    org.springframework.data.domain.Page<AbilitySpell> findByCampaignId(Long campaignId, org.springframework.data.domain.Pageable pageable);
}

