package com.violeta.fia.dto;

import com.violeta.fia.domain.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SancionRequest(
        Long pilotoId,
        Long escuderiaId,
        @NotNull(message = "La categoria es obligatoria") Categoria categoria,
        @NotNull(message = "La carrera es obligatoria") Long eventoId,
        @NotBlank(message = "El motivo es obligatorio") String motivo,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        @NotBlank(message = "La penalidad es obligatoria") String tipo,
        String descripcion
) {
}
