package com.violeta.fia.dto;

public record MiembroEscuderiaResponse(
        Long usuarioId,
        String nombre,
        String apellido,
        String email,
        String username,
        String rolGlobal,
        String rolEscuderia,
        String rolEscuderiaDescripcion,
        boolean puedeActualizarInformacion
) {
}
