package com.violeta.fia.dto;

public record LoginResponse(
        String token,
        String tipo,
        String username,
        String rol,
        long expiraEn
) {
}
