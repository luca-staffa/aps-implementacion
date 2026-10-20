package com.violeta.fia.dto;

public record EscuderiaResponse(
        Long id,
        String nombre,
        String pais,
        boolean activa
) {
}
