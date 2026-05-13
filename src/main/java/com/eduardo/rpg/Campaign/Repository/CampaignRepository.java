package com.eduardo.rpg.Campaign.Repository;

import com.eduardo.rpg.Campaign.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByMasterId(Long masterId);

    boolean existsByNameAndMasterId(String name, Long masterId);

    boolean existsByNameAndMasterIdAndIdNot(String name, Long masterId, Long id);
}

