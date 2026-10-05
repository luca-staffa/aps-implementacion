package com.violeta.fia.dto;

import com.violeta.fia.domain.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SancionRequest(
        Long pilotoId,
        Long escuderiaId,
        Categoria categoria,
        @NotBlank(message = "El motivo es obligatorio") String motivo,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        String tipo,
        String descripcion
) {
}
