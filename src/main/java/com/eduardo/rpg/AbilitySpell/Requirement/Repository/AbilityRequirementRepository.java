package com.eduardo.rpg.AbilitySpell.Requirement.Repository;

import com.eduardo.rpg.AbilitySpell.Requirement.AbilityRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbilityRequirementRepository extends JpaRepository<AbilityRequirement, Long> {

}

