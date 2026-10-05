package com.violeta.fia.dto;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String username,
        String rol,
        boolean activo,
        LocalDateTime ultimaActividad
) {
}
