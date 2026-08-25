package com.eduardo.rpg.CharacterClass;

import com.eduardo.rpg.Character.Character;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_classes")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CharacterClass {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "strength_bonus", nullable = false)
    private Integer strengthBonus = 0;

    @Column(name = "dexterity_bonus", nullable = false)
    private Integer dexterityBonus = 0;

    @Column(name = "constitution_bonus", nullable = false)
    private Integer constitutionBonus = 0;

    @Column(name = "intelligence_bonus", nullable = false)
    private Integer intelligenceBonus = 0;

    @Column(name = "wisdom_bonus", nullable = false)
    private Integer wisdomBonus = 0;

    @Column(name = "charisma_bonus", nullable = false)
    private Integer charismaBonus = 0;

    @Column(name = "skill_bonus_name", length = 50)
    private String skillBonusName;

    @Column(name = "skill_bonus_value", nullable = false)
    private Integer skillBonusValue = 0;

    @OneToMany(mappedBy = "characterClass", fetch = FetchType.LAZY)
    private List<Character> characters = new ArrayList<>();

    @Column(name = "campaign_id")
    private Long campaignId;

    // Habilidades / magias disponíveis para esta classe
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_class_abilities",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "ability_id")
    )
    private List<com.eduardo.rpg.AbilitySpell.AbilitySpell> abilities = new ArrayList<>();

    @Column(updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

