package com.eduardo.rpg.Grid.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateGridRequest(

    @NotNull(message = "Rows is required")
    @Min(value = 1, message = "Rows must be at least 1")
    @Max(value = 200, message = "Rows must be at most 200")
    Integer rows,

    @NotNull(message = "Cols is required")
    @Min(value = 1, message = "Cols must be at least 1")
    @Max(value = 200, message = "Cols must be at most 200")
    Integer cols
) {}
