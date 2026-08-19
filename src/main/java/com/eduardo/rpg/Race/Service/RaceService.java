package com.eduardo.rpg.Race.Service;

import com.eduardo.rpg.Race.Race;
import com.eduardo.rpg.Race.DTO.CreateRaceRequest;
import com.eduardo.rpg.Race.DTO.RaceMapper;
import com.eduardo.rpg.Race.DTO.RaceResponseDTO;
import com.eduardo.rpg.Race.DTO.UpdateRaceRequest;
import com.eduardo.rpg.Race.Repository.RaceRepository;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import com.eduardo.rpg.security.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final RaceRepository raceRepository;
    private final RaceMapper raceMapper;
    private final AccessControlService accessControlService;

    @Transactional
    public RaceResponseDTO createRace(Authentication authentication, CreateRaceRequest dto) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        accessControlService.requireGameContentWritePermission(authUser, dto.campaignId());

        if (raceRepository.existsByNameIgnoreCase(dto.name())) {
            throw new IllegalArgumentException("Já existe uma raça com este nome");
        }

        Race race = raceMapper.toEntity(dto);
        Race savedRace = raceRepository.save(race);
        return raceMapper.toResponse(savedRace);
    }

    @Transactional(readOnly = true)
    public RaceResponseDTO findRaceById(Long id) {
        if (id == null) {
            throw new ResourceNotFoundException("Raça não encontrada!");
        }

        Race race = raceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada!"));
        return raceMapper.toResponse(race);
    }

    @Transactional(readOnly = true)
    public Page<RaceResponseDTO> findAllRaces(Long campaignId, Pageable pageable) {
        if (campaignId != null) {
            return raceRepository.findByCampaignId(campaignId, pageable)
                .map(raceMapper::toResponse);
        }
        return raceRepository.findAll(pageable)
            .map(raceMapper::toResponse);
    }

    @Transactional
    public RaceResponseDTO updateRace(Authentication authentication, Long id, UpdateRaceRequest dto) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        Race race = raceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada!"));
        accessControlService.requireGameContentWritePermission(authUser, race.getCampaignId());

        raceRepository.findByNameIgnoreCase(dto.name())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Já existe uma raça com este nome");
            });

        race = raceMapper.toEntity(dto, race);
        Race updatedRace = raceRepository.save(race);
        return raceMapper.toResponse(updatedRace);
    }

    @Transactional
    public void deleteRace(Authentication authentication, Long id) {
        var authUser = accessControlService.getAuthenticatedUser(authentication);
        Race race = raceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada!"));
        accessControlService.requireGameContentWritePermission(authUser, race.getCampaignId());
        raceRepository.delete(race);
    }
}

