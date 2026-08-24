package com.eduardo.rpg.Grid.DTO;

public record GridTokenResponseDTO(
    Long id,
    Integer x,
    Integer y,
    String label,
    String color,
    Long characterId
) {}
