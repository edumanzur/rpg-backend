package com.eduardo.rpg.AbilitySpell.Repository;

import static org.junit.jupiter.api.Assertions.*;

import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import com.eduardo.rpg.AbilitySpell.Requirement.AbilityRequirement;
import com.eduardo.rpg.AbilitySpell.Requirement.Repository.AbilityRequirementRepository;
import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterClass.Repository.CharacterClassRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AbilitySpellRepositoryTest {

    @Autowired
    private AbilitySpellRepository abilitySpellRepository;

    @Autowired
    private CharacterClassRepository characterClassRepository;

    @Autowired
    private AbilityRequirementRepository abilityRequirementRepository;

    @Test
    void testCreateAbilityAndAssociateWithClassAndRequirement() {
        CharacterClass cls = new CharacterClass();
        cls.setName("TestClass");
        cls = characterClassRepository.save(cls);

        AbilitySpell ability = new AbilitySpell();
        ability.setName("Fireball");
        ability.setDamage("3d6");
        ability.setDescription("Explosive fireball");
        ability = abilitySpellRepository.save(ability);

        // associate
        cls.getAbilities().add(ability);
        characterClassRepository.save(cls);

        AbilitySpell loaded = abilitySpellRepository.findById(ability.getId()).orElseThrow();
        assertEquals("Fireball", loaded.getName());

        // create requirement
        AbilityRequirement req = new AbilityRequirement();
        req.setAbility(ability);
        req.setRequiredClass(cls);
        req.setMinLevel(3);
        abilityRequirementRepository.save(req);

        AbilityRequirement fetchedReq = abilityRequirementRepository.findById(req.getId()).orElseThrow();
        assertEquals(3, fetchedReq.getMinLevel());
    }
}

