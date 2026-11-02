package com.violeta.fia.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ControlTecnicoResponse(
        Long id,
        Long escuderiaId,
        String escuderiaNombre,
        Long pilotoId,
        String pilotoNombre,
        String auto,
        LocalDate fecha,
        String elementosVerificados,
        String resultado,
        String observaciones,
        LocalDateTime fechaCreacion
) {
}
