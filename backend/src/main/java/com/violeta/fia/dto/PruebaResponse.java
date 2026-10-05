package com.violeta.fia.dto;

import java.time.LocalDate;
import java.util.List;

public record PruebaResponse(
        Long id,
        Integer anio,
        String circuito,
        String pais,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String categorias,
        boolean avisoPublicado,
        List<ResultadoResponse> resultados
) {
}
