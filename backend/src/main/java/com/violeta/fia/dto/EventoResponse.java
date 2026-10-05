package com.violeta.fia.dto;

import com.violeta.fia.domain.Categoria;

import java.time.LocalDate;

public record EventoResponse(
        Long id,
        Categoria categoria,
        String nombre,
        String circuito,
        String pais,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String tipo
) {
}
