package com.eduardo.rpg.Race.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.eduardo.rpg.Race.Race;
import com.eduardo.rpg.Race.DTO.CreateRaceRequest;
import com.eduardo.rpg.Race.DTO.RaceMapper;
import com.eduardo.rpg.Race.DTO.RaceResponseDTO;
import com.eduardo.rpg.Race.DTO.UpdateRaceRequest;
import com.eduardo.rpg.Race.Repository.RaceRepository;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@DisplayName("RaceService Unit Tests")
@ExtendWith(MockitoExtension.class)
class RaceServiceTest {

    @Mock
    private RaceRepository raceRepository;

    @Mock
    private RaceMapper raceMapper;

    @InjectMocks
    private RaceService raceService;

    private Race race;
    private RaceResponseDTO raceResponseDTO;
    private CreateRaceRequest createRaceRequest;

    @BeforeEach
    void setUp() {
        race = new Race();
        race.setId(1L);
        race.setName("Human");
        race.setDescription("Versatile and resilient");
        race.setStrengthBonus(1);
        race.setDexterityBonus(1);
        race.setConstitutionBonus(0);
        race.setIntelligenceBonus(0);
        race.setWisdomBonus(0);
        race.setCharismaBonus(1);

        raceResponseDTO = new RaceResponseDTO(1L, "Human", "Versatile and resilient", 1, 1, 0, 0, 0, 1, null, null);
        createRaceRequest = new CreateRaceRequest("Human", "Versatile and resilient", 1, 1, 0, 0, 0, 1);
    }

    @Test
    @DisplayName("Should create race successfully")
    void testCreateRaceSuccess() {
        Race newRace = new Race();
        when(raceRepository.existsByNameIgnoreCase("Human")).thenReturn(false);
        when(raceMapper.toEntity(createRaceRequest)).thenReturn(newRace);
        when(raceRepository.save(any(Race.class))).thenReturn(race);
        when(raceMapper.toResponse(race)).thenReturn(raceResponseDTO);

        RaceResponseDTO result = raceService.createRace(createRaceRequest);

        assertNotNull(result);
        assertEquals("Human", result.name());
        verify(raceRepository, times(1)).save(any(Race.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when race name exists")
    void testCreateRaceNameExists() {
        when(raceRepository.existsByNameIgnoreCase("Human")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> raceService.createRace(createRaceRequest));
        verify(raceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when id is null")
    void testFindRaceByIdNull() {
        assertThrows(ResourceNotFoundException.class, () -> raceService.findRaceById(null));
    }

    @Test
    @DisplayName("Should find race by id successfully")
    void testFindRaceByIdSuccess() {
        when(raceRepository.findById(1L)).thenReturn(Optional.of(race));
        when(raceMapper.toResponse(race)).thenReturn(raceResponseDTO);

        RaceResponseDTO result = raceService.findRaceById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Human", result.name());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when race not found")
    void testFindRaceByIdNotFound() {
        when(raceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> raceService.findRaceById(1L));
    }

    @Test
    @DisplayName("Should find all races successfully")
    void testFindAllRacesSuccess() {
        Race second = new Race();
        second.setId(2L);
        second.setName("Elf");
        second.setDescription("Graceful and wise");

        when(raceRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(race, second)));
        when(raceMapper.toResponse(race)).thenReturn(raceResponseDTO);
        when(raceMapper.toResponse(second)).thenReturn(new RaceResponseDTO(2L, "Elf", "Graceful and wise", 0, 2, 0, 0, 1, 0, null, null));

        Page<RaceResponseDTO> result = raceService.findAllRaces(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Should update race successfully")
    void testUpdateRaceSuccess() {
        UpdateRaceRequest updateRequest = new UpdateRaceRequest("Human Updated", "Updated description", 2, 1, 0, 0, 0, 1);
        Race updatedRace = new Race();
        updatedRace.setId(1L);
        updatedRace.setName("Human Updated");

        when(raceRepository.findById(1L)).thenReturn(Optional.of(race));
        when(raceRepository.findByNameIgnoreCase("Human Updated")).thenReturn(Optional.empty());
        when(raceMapper.toEntity(updateRequest, race)).thenReturn(updatedRace);
        when(raceRepository.save(any(Race.class))).thenReturn(updatedRace);
        when(raceMapper.toResponse(updatedRace)).thenReturn(new RaceResponseDTO(1L, "Human Updated", "Updated description", 2, 1, 0, 0, 0, 1, null, null));

        RaceResponseDTO result = raceService.updateRace(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Human Updated", result.name());
        verify(raceRepository, times(1)).save(any(Race.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating to duplicate race name")
    void testUpdateRaceDuplicateName() {
        UpdateRaceRequest updateRequest = new UpdateRaceRequest("Elf", "Updated description", 0, 2, 0, 0, 1, 0);
        Race anotherRace = new Race();
        anotherRace.setId(2L);
        anotherRace.setName("Elf");

        when(raceRepository.findById(1L)).thenReturn(Optional.of(race));
        when(raceRepository.findByNameIgnoreCase("Elf")).thenReturn(Optional.of(anotherRace));

        assertThrows(IllegalArgumentException.class, () -> raceService.updateRace(1L, updateRequest));
    }

    @Test
    @DisplayName("Should delete race successfully")
    void testDeleteRaceSuccess() {
        when(raceRepository.findById(1L)).thenReturn(Optional.of(race));

        raceService.deleteRace(1L);

        verify(raceRepository, times(1)).delete(race);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent race")
    void testDeleteRaceNotFound() {
        when(raceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> raceService.deleteRace(1L));
    }
}

