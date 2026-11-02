package com.violeta.fia.dto;

import com.violeta.fia.domain.RolEscuderia;
import jakarta.validation.constraints.NotNull;

public record AsignarRolRequest(
        @NotNull(message = "El rol de escuderia es obligatorio") RolEscuderia rolEscuderia
) {
}
