package com.eduardo.rpg.AbilitySpell;

import com.eduardo.rpg.AbilitySpell.Requirement.AbilityRequirement;
import jakarta.persistence.*;
import com.eduardo.rpg.enums.CostType;
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

/**
 * Entidade que representa a junção de habilidades e magias.
 * Pode ser associada a várias classes e uma classe pode ter várias destas habilidades/magias.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_abilities")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AbilitySpell {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    // Dano expresso em notação de dados (ex: "2d8")
    @Column(name = "damage", length = 50)
    private String damage;

    // Texto descrevendo o efeito principal da habilidade/magia
    @Column(name = "effect", length = 500)
    private String effect;

    // Status principal (pode ser um atributo que a habilidade altera — usar String para flexibilidade)
    @Column(name = "main_status", length = 100)
    private String mainStatus;

    @Column(name = "description", length = 1000)
    private String description;

    // Custo da habilidade (tempo, espaço de magia, pontos, etc)
    @Column(name = "cost", length = 200)
    private String cost;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_type", length = 50)
    private CostType costType = CostType.TIME;

    // Nível mínimo necessário para usar a habilidade/magia
    @Column(name = "required_level")
    private Integer requiredLevel = 0;

    // Relacionamento com classes que possuem/percebem essa habilidade
    @ManyToMany(mappedBy = "abilities", fetch = FetchType.LAZY)
    private List<com.eduardo.rpg.CharacterClass.CharacterClass> classes = new ArrayList<>();

    @OneToMany(mappedBy = "ability", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AbilityRequirement> requirements = new ArrayList<>();

    @Column(updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}


