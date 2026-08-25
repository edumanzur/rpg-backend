package com.eduardo.rpg.Character;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.Race.Race;
import com.eduardo.rpg.CharacterClass.CharacterClass;
import com.eduardo.rpg.CharacterStatus.CharacterStatus;
import com.eduardo.rpg.enums.Gender;
import com.eduardo.rpg.enums.CharacterRole;
import com.eduardo.rpg.Equipment.Equipment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_characters")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Character {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 30)
    private Gender gender = Gender.UNSPECIFIED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private CharacterClass characterClass;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tb_character_equipments",
        joinColumns = @JoinColumn(name = "character_id"),
        inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private java.util.List<Equipment> equipments = new java.util.ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tb_character_abilities",
        joinColumns = @JoinColumn(name = "character_id"),
        inverseJoinColumns = @JoinColumn(name = "ability_id")
    )
    private java.util.List<com.eduardo.rpg.AbilitySpell.AbilitySpell> abilities = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CharacterStatus> statuses = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "character_role", nullable = false, length = 20)
    private CharacterRole role = CharacterRole.PLAYER;

    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Column(name = "experience", nullable = false)
    private Integer experience = 0;

    // Base ability scores chosen at character creation (before race/class/
    // equipment bonuses are added on top) — see AbilityScore computation in
    // the frontend's dnd.ts. Default 10 (no bonus, no penalty) for characters
    // created before this field existed.
    @Column(name = "strength_score", nullable = false)
    private Integer strengthScore = 10;

    @Column(name = "dexterity_score", nullable = false)
    private Integer dexterityScore = 10;

    @Column(name = "constitution_score", nullable = false)
    private Integer constitutionScore = 10;

    @Column(name = "intelligence_score", nullable = false)
    private Integer intelligenceScore = 10;

    @Column(name = "wisdom_score", nullable = false)
    private Integer wisdomScore = 10;

    @Column(name = "charisma_score", nullable = false)
    private Integer charismaScore = 10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Column(name = "description", length = 500)
    private String description;

    @Column(updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

