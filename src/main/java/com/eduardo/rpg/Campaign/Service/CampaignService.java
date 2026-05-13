package com.eduardo.rpg.Campaign.Service;

import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Campaign.DTO.CampaignMapper;
import com.eduardo.rpg.Campaign.DTO.CampaignResponseDTO;
import com.eduardo.rpg.Campaign.DTO.CreateCampaignRequest;
import com.eduardo.rpg.Campaign.DTO.UpdateCampaignRequest;
import com.eduardo.rpg.Campaign.Repository.CampaignRepository;
import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.User.Repository.UserRepository;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CampaignMapper campaignMapper;

    @Transactional
    public CampaignResponseDTO createCampaign(Long masterId, CreateCampaignRequest dto) {
        User master = userRepository.findById(masterId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        if (master.getRole() != Role.MASTER && master.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Somente MASTER ou ADMIN podem ser mestres de campanha");
        }

        if (campaignRepository.existsByNameAndMasterId(dto.name(), masterId)) {
            throw new IllegalArgumentException("Já existe uma campanha com este nome para este mestre");
        }

        Campaign campaign = campaignMapper.toEntity(dto);
        campaign.setMaster(master);

        Campaign savedCampaign = campaignRepository.save(campaign);
        return campaignMapper.toResponse(savedCampaign);
    }

    @Transactional(readOnly = true)
    public CampaignResponseDTO findCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        return campaignMapper.toResponse(campaign);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponseDTO> findCampaignsByMasterId(Long masterId) {
        userRepository.findById(masterId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado!"));

        return campaignRepository.findByMasterId(masterId)
            .stream()
            .map(campaignMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CampaignResponseDTO> findAllCampaigns() {
        return campaignRepository.findAll()
            .stream()
            .map(campaignMapper::toResponse)
            .toList();
    }

    @Transactional
    public CampaignResponseDTO updateCampaign(Long id, UpdateCampaignRequest dto) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));

        if (campaignRepository.existsByNameAndMasterIdAndIdNot(dto.name(), campaign.getMaster().getId(), id)) {
            throw new IllegalArgumentException("Já existe uma campanha com este nome para este mestre");
        }

        campaign = campaignMapper.toEntity(dto, campaign);
        Campaign updatedCampaign = campaignRepository.save(campaign);

        return campaignMapper.toResponse(updatedCampaign);
    }

    @Transactional
    public void deleteCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada!"));
        campaignRepository.delete(campaign);
    }
}

