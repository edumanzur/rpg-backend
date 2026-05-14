package com.eduardo.rpg.CharacterClass.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterClass.DTO.CharacterClassMapper;
import com.eduardo.rpg.CharacterClass.DTO.CharacterClassResponseDTO;
import com.eduardo.rpg.CharacterClass.DTO.CreateCharacterClassRequest;
import com.eduardo.rpg.CharacterClass.DTO.UpdateCharacterClassRequest;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import com.eduardo.rpg.AbilitySpell.Repository.AbilitySpellRepository;
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

@DisplayName("CharacterClassService Unit Tests")
@ExtendWith(MockitoExtension.class)
class CharacterClassServiceTest {

    @Mock
    private CharacterClassRepository characterClassRepository;

    @Mock
    private AbilitySpellRepository abilitySpellRepository;

    @Mock
    private CharacterClassMapper characterClassMapper;

    @InjectMocks
    private CharacterClassService characterClassService;

    private CharacterClass characterClass;
    private AbilitySpell abilitySpell;
    private CharacterClassResponseDTO characterClassResponseDTO;
    private CreateCharacterClassRequest createCharacterClassRequest;

    @BeforeEach
    void setUp() {
        characterClass = new CharacterClass();
        characterClass.setId(1L);
        characterClass.setName("Ranger");
        characterClass.setDescription("Skilled wilderness fighter");
        characterClass.setStrengthBonus(1);
        characterClass.setDexterityBonus(2);
        characterClass.setConstitutionBonus(0);
        characterClass.setIntelligenceBonus(0);
        characterClass.setWisdomBonus(1);
        characterClass.setCharismaBonus(0);

        abilitySpell = new AbilitySpell();
        abilitySpell.setId(1L);
        abilitySpell.setName("Fireball");
        abilitySpell.setDamage("3d6");
        abilitySpell.setEffect("Explosive fire damage");

        characterClassResponseDTO = new CharacterClassResponseDTO(1L, "Ranger", "Skilled wilderness fighter", 1, 2, 0, 0, 1, 0, null, null);
        createCharacterClassRequest = new CreateCharacterClassRequest("Ranger", "Skilled wilderness fighter", 1, 2, 0, 0, 1, 0);
    }

    @Test
    @DisplayName("Should create class successfully")
    void testCreateCharacterClassSuccess() {
        CharacterClass newClass = new CharacterClass();
        when(characterClassRepository.existsByNameIgnoreCase("Ranger")).thenReturn(false);
        when(characterClassMapper.toEntity(createCharacterClassRequest)).thenReturn(newClass);
        when(characterClassRepository.save(any(CharacterClass.class))).thenReturn(characterClass);
        when(characterClassMapper.toResponse(characterClass)).thenReturn(characterClassResponseDTO);

        CharacterClassResponseDTO result = characterClassService.createCharacterClass(createCharacterClassRequest);

        assertNotNull(result);
        assertEquals("Ranger", result.name());
        verify(characterClassRepository, times(1)).save(any(CharacterClass.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when class name exists")
    void testCreateCharacterClassNameExists() {
        when(characterClassRepository.existsByNameIgnoreCase("Ranger")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> characterClassService.createCharacterClass(createCharacterClassRequest));
        verify(characterClassRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when id is null")
    void testFindCharacterClassByIdNull() {
        assertThrows(ResourceNotFoundException.class, () -> characterClassService.findCharacterClassById(null));
    }

    @Test
    @DisplayName("Should find class by id successfully")
    void testFindCharacterClassByIdSuccess() {
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(characterClass));
        when(characterClassMapper.toResponse(characterClass)).thenReturn(characterClassResponseDTO);

        CharacterClassResponseDTO result = characterClassService.findCharacterClassById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Ranger", result.name());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when class not found")
    void testFindCharacterClassByIdNotFound() {
        when(characterClassRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> characterClassService.findCharacterClassById(1L));
    }

    @Test
    @DisplayName("Should find all classes successfully")
    void testFindAllCharacterClassesSuccess() {
        CharacterClass second = new CharacterClass();
        second.setId(2L);
        second.setName("Mage");
        second.setDescription("Arcane specialist");

        when(characterClassRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(characterClass, second)));
        when(characterClassMapper.toResponse(characterClass)).thenReturn(characterClassResponseDTO);
        when(characterClassMapper.toResponse(second)).thenReturn(new CharacterClassResponseDTO(2L, "Mage", "Arcane specialist", 0, 0, 0, 2, 1, 0, null, null));

        Page<CharacterClassResponseDTO> result = characterClassService.findAllCharacterClasses(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Should update class successfully")
    void testUpdateCharacterClassSuccess() {
        UpdateCharacterClassRequest updateRequest = new UpdateCharacterClassRequest("Ranger Updated", "Updated description", 2, 2, 0, 0, 1, 0);
        CharacterClass updatedCharacterClass = new CharacterClass();
        updatedCharacterClass.setId(1L);
        updatedCharacterClass.setName("Ranger Updated");

        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(characterClass));
        when(characterClassRepository.findByNameIgnoreCase("Ranger Updated")).thenReturn(Optional.empty());
        when(characterClassMapper.toEntity(updateRequest, characterClass)).thenReturn(updatedCharacterClass);
        when(characterClassRepository.save(any(CharacterClass.class))).thenReturn(updatedCharacterClass);
        when(characterClassMapper.toResponse(updatedCharacterClass)).thenReturn(new CharacterClassResponseDTO(1L, "Ranger Updated", "Updated description", 2, 2, 0, 0, 1, 0, null, null));

        CharacterClassResponseDTO result = characterClassService.updateCharacterClass(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Ranger Updated", result.name());
        verify(characterClassRepository, times(1)).save(any(CharacterClass.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating to duplicate class name")
    void testUpdateCharacterClassDuplicateName() {
        UpdateCharacterClassRequest updateRequest = new UpdateCharacterClassRequest("Mage", "Updated description", 0, 0, 0, 2, 1, 0);
        CharacterClass anotherClass = new CharacterClass();
        anotherClass.setId(2L);
        anotherClass.setName("Mage");

        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(characterClass));
        when(characterClassRepository.findByNameIgnoreCase("Mage")).thenReturn(Optional.of(anotherClass));

        assertThrows(IllegalArgumentException.class, () -> characterClassService.updateCharacterClass(1L, updateRequest));
    }

    @Test
    @DisplayName("Should delete class successfully")
    void testDeleteCharacterClassSuccess() {
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(characterClass));

        characterClassService.deleteCharacterClass(1L);

        verify(characterClassRepository, times(1)).delete(characterClass);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent class")
    void testDeleteCharacterClassNotFound() {
        when(characterClassRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> characterClassService.deleteCharacterClass(1L));
    }

    @Test
    @DisplayName("Should add ability to class successfully")
    void testAddAbilityToClassSuccess() {
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(characterClass));
        when(abilitySpellRepository.findById(1L)).thenReturn(Optional.of(abilitySpell));

        characterClassService.addAbilityToClass(1L, 1L);

        assertEquals(1, characterClass.getAbilities().size());
        assertTrue(characterClass.getAbilities().contains(abilitySpell));
        verify(characterClassRepository, times(1)).save(characterClass);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when adding duplicated ability to class")
    void testAddAbilityToClassDuplicate() {
        characterClass.getAbilities().add(abilitySpell);
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(characterClass));
        when(abilitySpellRepository.findById(1L)).thenReturn(Optional.of(abilitySpell));

        assertThrows(IllegalArgumentException.class, () -> characterClassService.addAbilityToClass(1L, 1L));
        verify(characterClassRepository, never()).save(any());
    }
}

