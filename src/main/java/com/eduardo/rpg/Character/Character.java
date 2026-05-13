package com.eduardo.rpg.Character;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.eduardo.rpg.User.Domains.User;
import com.eduardo.rpg.Campaign.Campaign;
import com.eduardo.rpg.enums.CharacterRole;

import java.time.LocalDateTime;

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

    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank
    @Column(name = "race", nullable = false, length = 50)
    private String race;

    @NotBlank
    @Column(name = "class_character", nullable = false, length = 50)
    private String classCharacter;

    @Enumerated(EnumType.STRING)
    @Column(name = "character_role", nullable = false, length = 20)
    private CharacterRole role = CharacterRole.PLAYER;

    @Min(value = 1)
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Min(value = 1)
    @Column(name = "experience", nullable = false)
    private Integer experience = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = true)
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

