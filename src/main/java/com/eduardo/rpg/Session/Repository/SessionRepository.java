package com.eduardo.rpg.Session.Repository;

import com.eduardo.rpg.Session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByCampaignId(Long campaignId);

    boolean existsByTitleAndCampaignId(String title, Long campaignId);

    boolean existsByTitleAndCampaignIdAndIdNot(String title, Long campaignId, Long id);
}

