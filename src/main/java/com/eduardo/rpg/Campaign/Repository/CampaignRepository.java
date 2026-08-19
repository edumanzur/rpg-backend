package com.eduardo.rpg.Campaign.Repository;

import com.eduardo.rpg.Campaign.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByMasterId(Long masterId);

    Page<Campaign> findByMasterId(Long masterId, Pageable pageable);

    List<Campaign> findByPlayersContaining(com.eduardo.rpg.User.Domains.User player);

    Page<Campaign> findByPlayersContaining(com.eduardo.rpg.User.Domains.User player, Pageable pageable);

    boolean existsByNameAndMasterId(String name, Long masterId);

    boolean existsByNameAndMasterIdAndIdNot(String name, Long masterId, Long id);

    Optional<Campaign> findByInviteCode(String inviteCode);
}

