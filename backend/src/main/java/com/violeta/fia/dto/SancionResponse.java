package com.violeta.fia.dto;

import com.violeta.fia.domain.Categoria;

import java.time.LocalDate;
import java.util.List;

public record SancionResponse(
        Long id,
        Long pilotoId,
        String pilotoNombre,
        Long escuderiaId,
        String escuderiaNombre,
        Categoria categoria,
        Long eventoId,
        String eventoNombre,
        String eventoCircuito,
        String motivo,
        LocalDate fecha,
        String tipo,
        String descripcion,
        int cantidadVistas,
        List<VistoResponse> vistas
) {
}
