package com.eduardo.rpg.AbilitySpell.Requirement;

import com.eduardo.rpg.AbilitySpell.AbilitySpell;
import com.eduardo.rpg.CharacterClass.CharacterClass;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_ability_requirements")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AbilityRequirement {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ability_id", nullable = false)
    private AbilitySpell ability;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private CharacterClass requiredClass;

    // Minimum level required to use the ability
    @Column(name = "min_level")
    private Integer minLevel;

    // Minimum stats required
    @Column(name = "min_strength")
    private Integer minStrength;

    @Column(name = "min_dexterity")
    private Integer minDexterity;

    @Column(name = "min_constitution")
    private Integer minConstitution;

    @Column(name = "min_intelligence")
    private Integer minIntelligence;

    @Column(name = "min_wisdom")
    private Integer minWisdom;

    @Column(name = "min_charisma")
    private Integer minCharisma;
}

