package com.eduardo.rpg.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.GenerationType;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Character {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String race;

    private String classCharacter;

    private Integer level;

    private Campaign campaign;

    private User user;

    private List<String> stats;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
