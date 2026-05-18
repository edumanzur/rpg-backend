package com.eduardo.rpg.Equipment.Service;

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

import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.Equipment.DTO.CreateEquipmentRequest;
import com.eduardo.rpg.Equipment.DTO.EquipmentMapper;
import com.eduardo.rpg.Equipment.DTO.EquipmentResponseDTO;
import com.eduardo.rpg.Equipment.DTO.UpdateEquipmentRequest;
import com.eduardo.rpg.Equipment.Equipment;
import com.eduardo.rpg.Equipment.Repository.EquipmentRepository;
import com.eduardo.rpg.enums.EquipmentType;
import com.eduardo.rpg.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@DisplayName("EquipmentService Unit Tests")
@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private CharacterClassRepository characterClassRepository;

    @Mock
    private EquipmentMapper equipmentMapper;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment equipment;
    private EquipmentResponseDTO equipmentResponseDTO;
    private CreateEquipmentRequest createEquipmentRequest;
    private CharacterClass warriorClass;

    @BeforeEach
    void setUp() {
        warriorClass = new CharacterClass();
        warriorClass.setId(1L);
        warriorClass.setName("Warrior");

        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Long Sword");
        equipment.setDescription("A sturdy sword");
        equipment.setType(EquipmentType.WEAPON);
        equipment.setDamage("2d8");
        equipment.setStrengthBonus(2);
        equipment.setDexterityBonus(0);
        equipment.setConstitutionBonus(0);
        equipment.setIntelligenceBonus(0);
        equipment.setWisdomBonus(0);
        equipment.setCharismaBonus(0);

        equipmentResponseDTO = new EquipmentResponseDTO(
            1L,
            "Long Sword",
            "A sturdy sword",
            EquipmentType.WEAPON,
            "2d8",
            2,
            0,
            0,
            0,
            0,
            0,
            List.of(new EquipmentResponseDTO.RequirementDTO(1L, 1L, "Warrior", 16, 0, 0, 0, 0, 0)),
            null,
            null
        );

        createEquipmentRequest = new CreateEquipmentRequest(
            "Long Sword",
            "A sturdy sword",
            EquipmentType.WEAPON,
            "2d8",
            2,
            0,
            0,
            0,
            0,
            0,
            List.of(new CreateEquipmentRequest.RequirementRequest(1L, 16, 0, 0, 0, 0, 0))
        );
    }

    @Test
    @DisplayName("Should create equipment successfully")
    void testCreateEquipmentSuccess() {
        when(equipmentRepository.findByNameIgnoreCase("Long Sword")).thenReturn(Optional.empty());
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(warriorClass));
        when(equipmentMapper.toEntity(createEquipmentRequest)).thenReturn(new Equipment());
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(equipmentMapper.toResponse(any(Equipment.class))).thenReturn(equipmentResponseDTO);

        EquipmentResponseDTO result = equipmentService.createEquipment(createEquipmentRequest);

        assertNotNull(result);
        assertEquals("Long Sword", result.name());

        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentRepository, times(1)).save(captor.capture());
        assertEquals(1, captor.getValue().getRequirements().size());
        assertEquals("Warrior", captor.getValue().getRequirements().get(0).getRequiredClass().getName());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when equipment name exists")
    void testCreateEquipmentNameExists() {
        when(equipmentRepository.findByNameIgnoreCase("Long Sword")).thenReturn(Optional.of(equipment));

        assertThrows(IllegalArgumentException.class, () -> equipmentService.createEquipment(createEquipmentRequest));
        verify(equipmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating weapon without damage")
    void testCreateWeaponWithoutDamage() {
        CreateEquipmentRequest request = new CreateEquipmentRequest(
            "Broken Sword",
            "A weapon without damage",
            EquipmentType.WEAPON,
            null,
            0,
            0,
            0,
            0,
            0,
            0,
            List.of()
        );

        assertThrows(IllegalArgumentException.class, () -> equipmentService.createEquipment(request));
    }


    @Test
    @DisplayName("Should find equipment by id successfully")
    void testFindEquipmentByIdSuccess() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(equipmentMapper.toResponse(equipment)).thenReturn(equipmentResponseDTO);

        EquipmentResponseDTO result = equipmentService.findEquipmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Long Sword", result.name());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when equipment not found")
    void testFindEquipmentByIdNotFound() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> equipmentService.findEquipmentById(1L));
    }

    @Test
    @DisplayName("Should find all equipments successfully")
    void testFindAllEquipmentsSuccess() {
        Equipment second = new Equipment();
        second.setId(2L);
        second.setName("Leather Armor");
        second.setType(EquipmentType.ARMOR);

        when(equipmentRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(equipment, second)));
        when(equipmentMapper.toResponse(equipment)).thenReturn(equipmentResponseDTO);
        when(equipmentMapper.toResponse(second)).thenReturn(new EquipmentResponseDTO(2L, "Leather Armor", "Light armor", EquipmentType.ARMOR, null, 0, 1, 2, 0, 0, 0, List.of(), null, null));

        Page<EquipmentResponseDTO> result = equipmentService.findAllEquipments(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Should update equipment successfully")
    void testUpdateEquipmentSuccess() {
        UpdateEquipmentRequest updateRequest = new UpdateEquipmentRequest(
            "Long Sword",
            "A sturdier sword",
            EquipmentType.WEAPON,
            "2d10",
            3,
            0,
            0,
            0,
            0,
            0,
            List.of(new UpdateEquipmentRequest.RequirementRequest(1L, 16, 0, 0, 0, 0, 0))
        );

        Equipment updatedEquipment = new Equipment();
        updatedEquipment.setId(1L);
        updatedEquipment.setName("Long Sword");

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.findByNameIgnoreCase("Long Sword")).thenReturn(Optional.of(equipment));
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(warriorClass));
        when(equipmentMapper.toEntity(updateRequest, equipment)).thenReturn(updatedEquipment);
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(equipmentMapper.toResponse(any(Equipment.class))).thenReturn(new EquipmentResponseDTO(1L, "Long Sword", "A sturdier sword", EquipmentType.WEAPON, "2d10", 3, 0, 0, 0, 0, 0, List.of(), null, null));

        EquipmentResponseDTO result = equipmentService.updateEquipment(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Long Sword", result.name());
        verify(equipmentRepository, times(1)).save(any(Equipment.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating to duplicate equipment name")
    void testUpdateEquipmentDuplicateName() {
        UpdateEquipmentRequest updateRequest = new UpdateEquipmentRequest(
            "Shield",
            "A shield",
            EquipmentType.ARMOR,
            null,
            0,
            0,
            0,
            0,
            0,
            0,
            List.of()
        );

        Equipment anotherEquipment = new Equipment();
        anotherEquipment.setId(2L);
        anotherEquipment.setName("Shield");

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.findByNameIgnoreCase("Shield")).thenReturn(Optional.of(anotherEquipment));

        assertThrows(IllegalArgumentException.class, () -> equipmentService.updateEquipment(1L, updateRequest));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when required class is missing")
    void testCreateEquipmentRequirementClassNotFound() {
        CreateEquipmentRequest request = new CreateEquipmentRequest(
            "Long Sword",
            "A sturdy sword",
            EquipmentType.WEAPON,
            "2d8",
            2,
            0,
            0,
            0,
            0,
            0,
            List.of(new CreateEquipmentRequest.RequirementRequest(99L, 16, 0, 0, 0, 0, 0))
        );

        when(equipmentRepository.findByNameIgnoreCase("Long Sword")).thenReturn(Optional.empty());
        when(characterClassRepository.findById(99L)).thenReturn(Optional.empty());
        when(equipmentMapper.toEntity(request)).thenReturn(new Equipment());

        assertThrows(ResourceNotFoundException.class, () -> equipmentService.createEquipment(request));
    }

    @Test
    @DisplayName("Should delete equipment successfully")
    void testDeleteEquipmentSuccess() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        equipmentService.deleteEquipment(1L);

        verify(equipmentRepository, times(1)).delete(equipment);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent equipment")
    void testDeleteEquipmentNotFound() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> equipmentService.deleteEquipment(1L));
    }
}


