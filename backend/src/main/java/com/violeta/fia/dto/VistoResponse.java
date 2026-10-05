package com.violeta.fia.dto;

import java.time.LocalDateTime;

public record VistoResponse(
        Long sancionId,
        Long usuarioId,
        String username,
        LocalDateTime fechaVisto,
        boolean visto
) {
}
