package com.eduardo.rpg.Equipment.Requirement.Repository;

import com.eduardo.rpg.Equipment.Requirement.EquipmentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRequirementRepository extends JpaRepository<EquipmentRequirement, Long> {
}

