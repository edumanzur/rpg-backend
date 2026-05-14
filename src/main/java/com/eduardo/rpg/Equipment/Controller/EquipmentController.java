package com.eduardo.rpg.Equipment.Controller;

import com.eduardo.rpg.Equipment.DTO.CreateEquipmentRequest;
import com.eduardo.rpg.Equipment.DTO.EquipmentResponseDTO;
import com.eduardo.rpg.Equipment.DTO.UpdateEquipmentRequest;
import com.eduardo.rpg.Equipment.Service.EquipmentService;
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
@RequestMapping("/equipments")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponseDTO> findEquipmentById(@PathVariable Long id) {
        EquipmentResponseDTO response = equipmentService.findEquipmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<EquipmentResponseDTO>> findAllEquipments(Pageable pageable) {
        Page<EquipmentResponseDTO> response = equipmentService.findAllEquipments(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<EquipmentResponseDTO> createEquipment(@RequestBody @Valid CreateEquipmentRequest dto) {
        EquipmentResponseDTO response = equipmentService.createEquipment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentResponseDTO> updateEquipment(
        @PathVariable Long id,
        @RequestBody @Valid UpdateEquipmentRequest dto) {
        EquipmentResponseDTO response = equipmentService.updateEquipment(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}

