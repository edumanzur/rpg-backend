package com.eduardo.rpg.Equipment;

import com.eduardo.rpg.Character.Character;
import com.eduardo.rpg.enums.EquipmentType;
import jakarta.persistence.*;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_equipments")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Equipment {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_type", nullable = false, length = 30)
    private EquipmentType type = EquipmentType.OTHER;

    // For weapons: damage expressed as dice notation (e.g., "2d8")
    @Column(name = "damage", length = 50)
    private String damage;

    // Stat bonuses provided by the equipment
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

    // Which characters currently have/equip this equipment (many-to-many)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tb_character_equipments",
        joinColumns = @JoinColumn(name = "equipment_id"),
        inverseJoinColumns = @JoinColumn(name = "character_id")
    )
    private List<Character> characters = new ArrayList<>();

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.eduardo.rpg.Equipment.Requirement.EquipmentRequirement> requirements = new ArrayList<>();

    @Column(updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

