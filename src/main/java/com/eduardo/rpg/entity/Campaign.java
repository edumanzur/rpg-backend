package com.eduardo.rpg.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    //Mestre da campanha
    private User master;

    //Lista de jogadores
    private List<User> players;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    //Status da campanha (Ativa ou não ativa)
    private Boolean status;
}
