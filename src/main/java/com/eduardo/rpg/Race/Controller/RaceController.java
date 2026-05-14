package com.eduardo.rpg.Race.Controller;

import com.eduardo.rpg.Race.DTO.CreateRaceRequest;
import com.eduardo.rpg.Race.DTO.RaceResponseDTO;
import com.eduardo.rpg.Race.DTO.UpdateRaceRequest;
import com.eduardo.rpg.Race.Service.RaceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/races")
public class RaceController {

    private final RaceService raceService;

    public RaceController(RaceService raceService) {
        this.raceService = raceService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<RaceResponseDTO> findRaceById(@PathVariable Long id) {
        RaceResponseDTO response = raceService.findRaceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<RaceResponseDTO>> findAllRaces(Pageable pageable) {
        Page<RaceResponseDTO> response = raceService.findAllRaces(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<RaceResponseDTO> createRace(@RequestBody @Valid CreateRaceRequest dto) {
        RaceResponseDTO response = raceService.createRace(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RaceResponseDTO> updateRace(
        @PathVariable Long id,
        @RequestBody @Valid UpdateRaceRequest dto) {
        RaceResponseDTO response = raceService.updateRace(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRace(@PathVariable Long id) {
        raceService.deleteRace(id);
        return ResponseEntity.noContent().build();
    }
}

