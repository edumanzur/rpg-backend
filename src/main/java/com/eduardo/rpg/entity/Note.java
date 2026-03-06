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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Campaign campaign;
    
    private String title;
    
    private String content;

    private LocalDateTime createdAt;

    private User createdBy;
}
