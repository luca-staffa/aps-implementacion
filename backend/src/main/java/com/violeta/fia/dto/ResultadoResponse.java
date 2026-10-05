package com.violeta.fia.dto;

public record ResultadoResponse(
        Long id,
        Long pruebaId,
        String escuderia,
        String piloto,
        String mejorTiempo,
        Integer vueltas,
        Integer posicion
) {
}
