package com.violeta.fia.dto;

public record PilotoPublicResponse(
        Long id,
        String nombre,
        String apellido,
        String nacionalidad,
        Integer numero,
        String abreviatura,
        String rol,
        String fotoUrl,
        Long categoriaId,
        String categoriaCodigo,
        Long temporadaId,
        Integer temporadaAnio,
        Long escuderiaId,
        String escuderiaNombre
) {
}
