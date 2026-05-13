package com.eduardo.rpg.StatusTemplate.Repository;

import com.eduardo.rpg.StatusTemplate.StatusTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusTemplateRepository extends JpaRepository<StatusTemplate, Long> {
    List<StatusTemplate> findByCampaignId(Long campaignId);
    Optional<StatusTemplate> findByIdAndCampaignId(Long id, Long campaignId);
    boolean existsByCampaignIdAndNameIgnoreCase(Long campaignId, String name);
    boolean existsByCampaignIdAndNameIgnoreCaseAndIdNot(Long campaignId, String name, Long id);
}

