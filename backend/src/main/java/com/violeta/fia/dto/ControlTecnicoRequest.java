package com.violeta.fia.dto;

import com.violeta.fia.domain.ResultadoControl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ControlTecnicoRequest(
        @NotNull(message = "La escuderia es obligatoria") Long escuderiaId,
        Long pilotoId,
        @NotBlank(message = "El auto es obligatorio") @Size(max = 150, message = "El auto no puede superar los 150 caracteres") String auto,
        @NotNull(message = "La fecha es obligatoria") @PastOrPresent(message = "La fecha no puede ser futura") LocalDate fecha,
        @NotBlank(message = "Los elementos verificados son obligatorios") @Size(max = 2000, message = "Los elementos verificados son demasiado largos") String elementosVerificados,
        @NotNull(message = "El resultado es obligatorio") ResultadoControl resultado,
        @Size(max = 2000, message = "Las observaciones son demasiado largas") String observaciones
) {
}
