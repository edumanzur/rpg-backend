package com.eduardo.rpg.Equipment.Repository;

import com.eduardo.rpg.Equipment.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
	Optional<Equipment> findByNameIgnoreCase(String name);
}

