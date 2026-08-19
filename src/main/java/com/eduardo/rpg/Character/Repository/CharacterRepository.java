package com.eduardo.rpg.Character.Repository;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.enums.CharacterRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    List<Character> findByUserId(Long userId);

    Page<Character> findByUserId(Long userId, Pageable pageable);

    List<Character> findByCampaignId(Long campaignId);

    List<Character> findByCampaignIdAndUserId(Long campaignId, Long userId);

    List<Character> findByCampaign_Master_IdOrUser_Id(Long masterId, Long userId);

    Page<Character> findByCampaign_Master_IdOrUser_Id(Long masterId, Long userId, Pageable pageable);

    boolean existsByNameAndUserId(String name, Long userId);

    boolean existsByUserIdAndCampaignIdAndRole(Long userId, Long campaignId, CharacterRole role);
}

