package com.eduardo.rpg.Race.Service;

import com.eduardo.rpg.Race.Race;
import com.eduardo.rpg.Race.DTO.CreateRaceRequest;
import com.eduardo.rpg.Race.DTO.RaceMapper;
import com.eduardo.rpg.Race.DTO.RaceResponseDTO;
import com.eduardo.rpg.Race.DTO.UpdateRaceRequest;
import com.eduardo.rpg.Race.Repository.RaceRepository;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final RaceRepository raceRepository;
    private final RaceMapper raceMapper;

    @Transactional
    public RaceResponseDTO createRace(CreateRaceRequest dto) {
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
    public Page<RaceResponseDTO> findAllRaces(Pageable pageable) {
        return raceRepository.findAll(pageable)
            .map(raceMapper::toResponse);
    }

    @Transactional
    public RaceResponseDTO updateRace(Long id, UpdateRaceRequest dto) {
        Race race = raceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada!"));

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
    public void deleteRace(Long id) {
        Race race = raceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Raça não encontrada!"));
        raceRepository.delete(race);
    }
}

