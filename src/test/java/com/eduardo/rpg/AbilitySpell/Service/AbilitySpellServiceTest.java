package com.eduardo.rpg.AbilitySpell.Service;

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

import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellMapper;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellRequest;
import com.eduardo.rpg.AbilitySpell.DTO.AbilitySpellResponseDTO;
import com.eduardo.rpg.AbilitySpell.Repository.AbilitySpellRepository;
import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import com.eduardo.rpg.enums.CostType;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.enums.Role;
import com.eduardo.rpg.security.AccessControlService;

@DisplayName("AbilitySpellService Unit Tests")
@ExtendWith(MockitoExtension.class)
class AbilitySpellServiceTest {

    @Mock
    private AbilitySpellRepository abilitySpellRepository;

    @Mock
    private CharacterClassRepository characterClassRepository;

    @Mock
    private AbilitySpellMapper abilitySpellMapper;

    @Mock
    private AccessControlService accessControlService;

    @InjectMocks
    private AbilitySpellService abilitySpellService;

    private AbilitySpell abilitySpell;
    private AbilitySpellResponseDTO abilitySpellResponseDTO;
    private AbilitySpellRequest createAbilitySpellRequest;
    private CharacterClass mageClass;
    private Authentication authentication;
    private User masterUser;

    @BeforeEach
    void setUp() {
        masterUser = new User(1L, "masteruser", "master@example.com", "password", Role.PLAYER, null, null);
        authentication = new UsernamePasswordAuthenticationToken("masteruser", "password", List.of(new SimpleGrantedAuthority("ROLE_MASTER")));
        mageClass = new CharacterClass();
        mageClass.setId(1L);
        mageClass.setName("Mage");

        abilitySpell = new AbilitySpell();
        abilitySpell.setId(1L);
        abilitySpell.setName("Fireball");
        abilitySpell.setDamage("3d6");
        abilitySpell.setEffect("Explosive fire damage");
        abilitySpell.setMainStatus("Burn");
        abilitySpell.setDescription("A powerful fire spell");
        abilitySpell.setCost("1 action");
        abilitySpell.setCostType(CostType.ACTION);
        abilitySpell.setRequiredLevel(3);

        abilitySpellResponseDTO = new AbilitySpellResponseDTO(
            1L,
            "Fireball",
            "3d6",
            "Explosive fire damage",
            "Burn",
            "A powerful fire spell",
            "1 action",
            CostType.ACTION,
            3,
            null,
            List.of(new AbilitySpellResponseDTO.RequirementDTO(1L, 1L, "Mage", 3, 0, 0, 0, 2, 0, 0)),
            null,
            null
        );

        createAbilitySpellRequest = new AbilitySpellRequest(
            "Fireball",
            "3d6",
            "Explosive fire damage",
            "Burn",
            "A powerful fire spell",
            "1 action",
            CostType.ACTION,
            3,
            null,
            List.of(new AbilitySpellRequest.RequirementRequest(1L, 3, 0, 0, 0, 2, 0, 0))
        );
    }

    @Test
    @DisplayName("Should create ability successfully")
    void testCreateAbilitySpellSuccess() {
        AbilitySpell newAbility = new AbilitySpell();
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(masterUser);
        when(abilitySpellRepository.findByNameIgnoreCase("Fireball")).thenReturn(Optional.empty());
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(mageClass));
        when(abilitySpellMapper.toEntity(createAbilitySpellRequest)).thenReturn(newAbility);
        when(abilitySpellRepository.save(any(AbilitySpell.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(abilitySpellMapper.toResponse(any(AbilitySpell.class))).thenReturn(abilitySpellResponseDTO);

        AbilitySpellResponseDTO result = abilitySpellService.createAbilitySpell(authentication, createAbilitySpellRequest);

        assertNotNull(result);
        assertEquals("Fireball", result.name());

        ArgumentCaptor<AbilitySpell> captor = ArgumentCaptor.forClass(AbilitySpell.class);
        verify(abilitySpellRepository, times(1)).save(captor.capture());
        assertEquals(1, captor.getValue().getRequirements().size());
        assertEquals("Mage", captor.getValue().getRequirements().get(0).getRequiredClass().getName());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ability name exists")
    void testCreateAbilitySpellNameExists() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(masterUser);
        when(abilitySpellRepository.findByNameIgnoreCase("Fireball")).thenReturn(Optional.of(abilitySpell));

        assertThrows(IllegalArgumentException.class, () -> abilitySpellService.createAbilitySpell(authentication, createAbilitySpellRequest));
        verify(abilitySpellRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when required class is missing")
    void testCreateAbilitySpellRequirementClassNotFound() {
        AbilitySpellRequest request = new AbilitySpellRequest(
            "Fireball",
            "3d6",
            "Explosive fire damage",
            "Burn",
            "A powerful fire spell",
            "1 action",
            CostType.ACTION,
            3,
            null,
            List.of(new AbilitySpellRequest.RequirementRequest(99L, 3, 0, 0, 0, 2, 0, 0))
        );

        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(masterUser);
        when(abilitySpellRepository.findByNameIgnoreCase("Fireball")).thenReturn(Optional.empty());
        when(characterClassRepository.findById(99L)).thenReturn(Optional.empty());
        when(abilitySpellMapper.toEntity(request)).thenReturn(new AbilitySpell());

        assertThrows(ResourceNotFoundException.class, () -> abilitySpellService.createAbilitySpell(authentication, request));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when requirement is null")
    void testCreateAbilitySpellNullRequirement() {
        List<AbilitySpellRequest.RequirementRequest> requirements = new java.util.AbstractList<>() {
            @Override
            public AbilitySpellRequest.RequirementRequest get(int index) {
                return null;
            }

            @Override
            public int size() {
                return 1;
            }
        };

        AbilitySpellRequest request = new AbilitySpellRequest(
            "Fireball",
            "3d6",
            "Explosive fire damage",
            "Burn",
            "A powerful fire spell",
            "1 action",
            CostType.ACTION,
            3,
            null,
            requirements
        );

        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(masterUser);
        when(abilitySpellRepository.findByNameIgnoreCase("Fireball")).thenReturn(Optional.empty());
        when(abilitySpellMapper.toEntity(request)).thenReturn(new AbilitySpell());

        assertThrows(IllegalArgumentException.class, () -> abilitySpellService.createAbilitySpell(authentication, request));
    }

    @Test
    @DisplayName("Should find ability by id successfully")
    void testFindAbilitySpellByIdSuccess() {
        when(abilitySpellRepository.findById(1L)).thenReturn(Optional.of(abilitySpell));
        when(abilitySpellMapper.toResponse(abilitySpell)).thenReturn(abilitySpellResponseDTO);

        AbilitySpellResponseDTO result = abilitySpellService.findAbilitySpellById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Fireball", result.name());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when ability not found")
    void testFindAbilitySpellByIdNotFound() {
        when(abilitySpellRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> abilitySpellService.findAbilitySpellById(1L));
    }

    @Test
    @DisplayName("Should find all abilities successfully")
    void testFindAllAbilitySpellsSuccess() {
        AbilitySpell second = new AbilitySpell();
        second.setId(2L);
        second.setName("Ice Bolt");
        when(abilitySpellMapper.toResponse(second)).thenReturn(new AbilitySpellResponseDTO(2L, "Ice Bolt", "2d8", "Cold damage", "Freeze", "A cold spell", "1 action", CostType.ACTION, 2, null, List.of(), null, null));

        when(abilitySpellRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(abilitySpell, second)));
        when(abilitySpellMapper.toResponse(abilitySpell)).thenReturn(abilitySpellResponseDTO);
        when(abilitySpellMapper.toResponse(second)).thenReturn(new AbilitySpellResponseDTO(2L, "Ice Bolt", "2d8", "Cold damage", "Freeze", "A cold spell", "1 action", CostType.ACTION, 2, null, List.of(), null, null));

        Page<AbilitySpellResponseDTO> result = abilitySpellService.findAllAbilitySpells(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Should update ability successfully")
    void testUpdateAbilitySpellSuccess() {
        AbilitySpellRequest updateRequest = new AbilitySpellRequest(
            "Fireball",
            "4d6",
            "Stronger fire damage",
            "Burn",
            "An improved fire spell",
            "1 action",
            CostType.ACTION,
            4,
            null,
            List.of(new AbilitySpellRequest.RequirementRequest(1L, 4, 0, 0, 0, 2, 0, 0))
        );

        AbilitySpell updatedAbility = new AbilitySpell();
        updatedAbility.setId(1L);
        updatedAbility.setName("Fireball");

        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(masterUser);
        when(abilitySpellRepository.findById(1L)).thenReturn(Optional.of(abilitySpell));
        when(abilitySpellRepository.findByNameIgnoreCase("Fireball")).thenReturn(Optional.of(abilitySpell));
        when(characterClassRepository.findById(1L)).thenReturn(Optional.of(mageClass));
        when(abilitySpellMapper.toEntity(updateRequest, abilitySpell)).thenReturn(updatedAbility);
        when(abilitySpellRepository.save(any(AbilitySpell.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(abilitySpellMapper.toResponse(any(AbilitySpell.class))).thenReturn(new AbilitySpellResponseDTO(1L, "Fireball", "4d6", "Stronger fire damage", "Burn", "An improved fire spell", "1 action", CostType.ACTION, 4, null, List.of(), null, null));

        AbilitySpellResponseDTO result = abilitySpellService.updateAbilitySpell(authentication, 1L, updateRequest);

        assertNotNull(result);
        assertEquals("Fireball", result.name());
        verify(abilitySpellRepository, times(1)).save(any(AbilitySpell.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating to duplicate ability name")
    void testUpdateAbilitySpellDuplicateName() {
        AbilitySpellRequest updateRequest = new AbilitySpellRequest(
            "Ice Bolt",
            "2d8",
            "Cold damage",
            "Freeze",
            "A cold spell",
            "1 action",
            CostType.ACTION,
            2,
            null,
            List.of()
        );

        AbilitySpell anotherAbility = new AbilitySpell();
        anotherAbility.setId(2L);
        anotherAbility.setName("Ice Bolt");

        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(masterUser);
        when(abilitySpellRepository.findById(1L)).thenReturn(Optional.of(abilitySpell));
        when(abilitySpellRepository.findByNameIgnoreCase("Ice Bolt")).thenReturn(Optional.of(anotherAbility));

        assertThrows(IllegalArgumentException.class, () -> abilitySpellService.updateAbilitySpell(authentication, 1L, updateRequest));
    }

    @Test
    @DisplayName("Should delete ability successfully")
    void testDeleteAbilitySpellSuccess() {
        when(accessControlService.getAuthenticatedUser(authentication)).thenReturn(masterUser);
        when(abilitySpellRepository.findById(1L)).thenReturn(Optional.of(abilitySpell));

        abilitySpellService.deleteAbilitySpell(authentication, 1L);

        verify(abilitySpellRepository, times(1)).delete(abilitySpell);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent ability")
    void testDeleteAbilitySpellNotFound() {
        when(abilitySpellRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> abilitySpellService.deleteAbilitySpell(authentication, 1L));
    }
}



