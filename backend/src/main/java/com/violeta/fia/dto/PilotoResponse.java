package com.violeta.fia.dto;

public record PilotoResponse(
        Long id,
        String nombre,
        String apellido,
        String fechaNacimiento,
        String nacionalidad,
        Integer numero,
        String abreviatura,
        String rol,
        String numeroLicenciaFia,
        String fotoUrl,
        boolean activo,
        Long categoriaId,
        String categoriaCodigo,
        Long temporadaId,
        Integer temporadaAnio,
        Long escuderiaId,
        String escuderiaNombre
) {
}
